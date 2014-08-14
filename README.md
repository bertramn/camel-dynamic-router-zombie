Camel Dynamic Router turns into a Zombie
===========================

This example is replicating a problem with the camel dynamic routing leaving the camel context and JVM in an ill state after a transacted route fails and the transaction manager also fails during the rollback.

The test case contains a custom transaction manager based on the Spring `TransactionManager`. It also contains a dynamic router. The idea is that the transaction manager will call a `SessionResourceManager` which routes the release of the session resource to a dynamically determined endpoint (in our case the implementation `OneSessionResourceRoute`). The target in `OneSessionResourceRoute` will fail with a SOAP Fault and cause the `TestTransactionManager` to fail during transaction rollback. 


The failure is simulated by unit test `TransactedRouteCascadeFailureTest`. This test will create 2 nested routes that are both transactional. The nested route will fail and trigger a cascading rollback. This triggers the `TestTransactionManager` to rollback.

So what happens when you run this test?

It all goes according to plan where the TestTransactionManager tries to rollback, calls the `SessionResourceManager` which uses the `DynamicSourceRouter` to send the request to release the session to a dynamic location in this test `OneSessionResourceRoute` which in turn produces a CXF SOAP Fault.

Then however the `DynamicSourceRouter` is somehow called again and the camel internals fall over with the following stack trace:

```
Exception in thread "default-workqueue-1" org.apache.camel.CamelExecutionException: Exception occurred during execution on the exchange: Exchange[Message: ]
	at org.apache.camel.util.ObjectHelper.wrapCamelExecutionException(ObjectHelper.java:1379)
	at org.apache.camel.builder.ExpressionBuilder$40.evaluate(ExpressionBuilder.java:989)
	at org.apache.camel.support.ExpressionAdapter.evaluate(ExpressionAdapter.java:36)
	at org.apache.camel.component.bean.MethodInfo$2.evaluateParameterBinding(MethodInfo.java:588)
	at org.apache.camel.component.bean.MethodInfo$2.evaluate(MethodInfo.java:478)
	at org.apache.camel.component.bean.MethodInfo$DynamicRouterExpression.evaluate(MethodInfo.java:92)
	at org.apache.camel.support.ExpressionAdapter.evaluate(ExpressionAdapter.java:36)
	at org.apache.camel.processor.DynamicRouter$DynamicRoutingSlipIterator.hasNext(DynamicRouter.java:67)
	at org.apache.camel.processor.RoutingSlip$2$1.done(RoutingSlip.java:312)
	at org.apache.camel.component.directvm.DirectVmProcessor$1.done(DirectVmProcessor.java:63)
	at org.apache.camel.processor.CamelInternalProcessor$InternalCallback.done(CamelInternalProcessor.java:251)
	at org.apache.camel.processor.Pipeline$1.done(Pipeline.java:145)
	at org.apache.camel.processor.CamelInternalProcessor$InternalCallback.done(CamelInternalProcessor.java:251)
	at org.apache.camel.processor.RedeliveryErrorHandler$1.done(RedeliveryErrorHandler.java:410)
	at org.apache.camel.processor.interceptor.Debug$1$1.done(Debug.java:56)
	at org.apache.camel.processor.interceptor.TraceInterceptor$1.done(TraceInterceptor.java:180)
	at org.apache.camel.processor.SendProcessor$1.done(SendProcessor.java:123)
	at org.apache.camel.component.cxf.CxfClientCallback.handleException(CxfClientCallback.java:93)
	at org.apache.cxf.interceptor.ClientOutFaultObserver.onMessage(ClientOutFaultObserver.java:59)
	at org.apache.cxf.endpoint.ClientImpl$1.onMessage(ClientImpl.java:561)
	at org.apache.cxf.transport.http.HTTPConduit$WrappedOutputStream$1.run(HTTPConduit.java:1142)
	at org.apache.cxf.workqueue.AutomaticWorkQueueImpl$3.run(AutomaticWorkQueueImpl.java:428)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at org.apache.cxf.workqueue.AutomaticWorkQueueImpl$AWQThreadFactory$1.run(AutomaticWorkQueueImpl.java:353)
	at java.lang.Thread.run(Thread.java:745)
Caused by: org.apache.camel.InvalidPayloadException: No body available of type: org.w3c.dom.Document but has value: org.apache.camel.component.cxf.CxfPayload@656f0446 of type: org.apache.camel.component.cxf.CxfPayload on: Message: . Caused by: No type converter available to convert from type: org.apache.camel.component.cxf.CxfPayload to the required type: org.w3c.dom.Document with value org.apache.camel.component.cxf.CxfPayload@656f0446. Exchange[Message: ]. Caused by: [org.apache.camel.NoTypeConversionAvailableException - No type converter available to convert from type: org.apache.camel.component.cxf.CxfPayload to the required type: org.w3c.dom.Document with value org.apache.camel.component.cxf.CxfPayload@656f0446]
	at org.apache.camel.impl.MessageSupport.getMandatoryBody(MessageSupport.java:101)
	at org.apache.camel.builder.ExpressionBuilder$40.evaluate(ExpressionBuilder.java:987)
	... 24 more
Caused by: org.apache.camel.NoTypeConversionAvailableException: No type converter available to convert from type: org.apache.camel.component.cxf.CxfPayload to the required type: org.w3c.dom.Document with value org.apache.camel.component.cxf.CxfPayload@656f0446
	at org.apache.camel.impl.converter.BaseTypeConverterRegistry.mandatoryConvertTo(BaseTypeConverterRegistry.java:181)
	at org.apache.camel.core.osgi.OsgiTypeConverter.mandatoryConvertTo(OsgiTypeConverter.java:116)
	at org.apache.camel.impl.MessageSupport.getMandatoryBody(MessageSupport.java:99)
	... 25 more
```

I know the error is related to the type casting on the dynamic router but a) the dynamic router should not be called again after the route failure and b) the type converter for CXF messages is available if calling the endpount with a success scenario, so why is it not available for the SOAP envelope with fault?

This is the router signature that triggers the failure when camel tries to jam the CXFPayload into @Body.

```java
@DynamicRouter
public String route(@Header(DynamicSourceRouter.SOURCE_ADAPTER) String adapterName,
		            @Header(Exchange.SLIP_ENDPOINT) String slip,
                    @Body Document body) {
}
```

I would expect that the session release error is maybe thrown up to the transaction manager or at least can be caught there but that does not happen. Once the code falls over in the dynamic router, the rollback function is never entered again when unwinding the call stack.

What is worse, once the test fails, the JVM hangs and needs to be killed. Running the test in an OSGi container will stuff the entire container and it has to be killed with -9.


To run the test on the command line, just do `mvn clean test`.

To run the JUnit test case manually for debugging, just import the project into your IDE (I use Eclipse) and start the SOAPUI mock manually.