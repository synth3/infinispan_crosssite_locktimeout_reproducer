Reproducer for `ISPN000299: Unable to acquire lock after 10 seconds` when using a relay to connect sites.

# Overview

- Infinispan-Configuration is located in `conf`
  - `conf/infinispan.transports.default.xml` contains the transports in use:
    - One TCP-Transport with TCPPing used for connection between the sites
    - One UDP-Transport with Multicast for communication inside of the sites
  - `conf/infinispan.default.crosssite.xml` contains the cache-, relay- and site-configuration, `conf/infinispan.transports.default.xml` is imported and the contained transports are used
    - There are two sites: `LON` and `NYC`
    - One distributed cache named `distributed-cache`
    - The relay is configured to have one site master on each site
- The main class is `testing.RelayTest`
  - It uses `conf/infinispan.default.crosssite.xml` to start multiple nodes for both sites
    - Node names and network-configuration is set via properties in `getCommonProps(...)`, `fillSiteNYCProps(...)` and `fillSiteLONProps(...)`
- Code for creating the cache container and accessing the distributed cache is located in `testing.TestClusterClientBase.performWithProps(...)`
- The console-output is configured to contain log messages with level >=`WARN`
- Log messages with level >=`INFO` are output to `log/the.log`

# Run

- run `./gradlew runRelayTest` or `gradlew.bat runRelayTest`
- After _some seconds up to three minutes_ writing values to a distributed cache fails with the following exception (example):
```log
org.infinispan.remoting.RemoteException: ISPN000217: Received exception from nyc_node_25, see cause for remote stack trace
	at org.infinispan.remoting.transport.ResponseCollectors.wrapRemoteException(ResponseCollectors.java:25)
	at org.infinispan.remoting.transport.ValidSingleResponseCollector.withException(ValidSingleResponseCollector.java:37)
	at org.infinispan.remoting.transport.ValidSingleResponseCollector.addResponse(ValidSingleResponseCollector.java:21)
	at org.infinispan.remoting.transport.impl.SingleTargetRequest.addResponse(SingleTargetRequest.java:73)
	at org.infinispan.remoting.transport.impl.SingleTargetRequest.onResponse(SingleTargetRequest.java:43)
	at org.infinispan.remoting.transport.impl.RequestRepository.addResponse(RequestRepository.java:52)
	at org.infinispan.remoting.transport.jgroups.JGroupsTransport.processResponse(JGroupsTransport.java:1402)
	at org.infinispan.remoting.transport.jgroups.JGroupsTransport.processMessage(JGroupsTransport.java:1305)
	at org.infinispan.remoting.transport.jgroups.JGroupsTransport.access$300(JGroupsTransport.java:131)
	at org.infinispan.remoting.transport.jgroups.JGroupsTransport$ChannelCallbacks.up(JGroupsTransport.java:1445)
	at org.jgroups.JChannel.up(JChannel.java:784)
	at org.jgroups.stack.ProtocolStack.up(ProtocolStack.java:913)
	at org.jgroups.protocols.relay.RELAY2.up(RELAY2.java:499)
	at org.jgroups.protocols.FRAG3.up(FRAG3.java:165)
	at org.jgroups.protocols.FlowControl.up(FlowControl.java:343)
	at org.jgroups.protocols.FlowControl.up(FlowControl.java:343)
	at org.jgroups.protocols.pbcast.GMS.up(GMS.java:876)
	at org.jgroups.protocols.pbcast.STABLE.up(STABLE.java:243)
	at org.jgroups.protocols.UNICAST3.deliverMessage(UNICAST3.java:1049)
	at org.jgroups.protocols.UNICAST3.addMessage(UNICAST3.java:772)
	at org.jgroups.protocols.UNICAST3.handleDataReceived(UNICAST3.java:753)
	at org.jgroups.protocols.UNICAST3.up(UNICAST3.java:405)
	at org.jgroups.protocols.pbcast.NAKACK2.up(NAKACK2.java:592)
	at org.jgroups.protocols.VERIFY_SUSPECT.up(VERIFY_SUSPECT.java:132)
	at org.jgroups.protocols.FailureDetection.up(FailureDetection.java:186)
	at org.jgroups.protocols.FD_SOCK.up(FD_SOCK.java:254)
	at org.jgroups.protocols.MERGE3.up(MERGE3.java:281)
	at org.jgroups.protocols.Discovery.up(Discovery.java:300)
	at org.jgroups.protocols.TP.passMessageUp(TP.java:1396)
	at org.jgroups.util.SubmitToThreadPool$SingleMessageHandler.run(SubmitToThreadPool.java:87)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1130)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:630)
	at java.base/java.lang.Thread.run(Thread.java:832)
	Suppressed: org.infinispan.commons.util.logging.TraceException
		at org.infinispan.interceptors.impl.SimpleAsyncInvocationStage.get(SimpleAsyncInvocationStage.java:39)
		at org.infinispan.interceptors.impl.AsyncInterceptorChainImpl.invoke(AsyncInterceptorChainImpl.java:246)
		at org.infinispan.cache.impl.InvocationHelper.doInvoke(InvocationHelper.java:298)
		at org.infinispan.cache.impl.InvocationHelper.invoke(InvocationHelper.java:102)
		at org.infinispan.cache.impl.InvocationHelper.invoke(InvocationHelper.java:84)
		at org.infinispan.cache.impl.CacheImpl.put(CacheImpl.java:1268)
		at org.infinispan.cache.impl.CacheImpl.put(CacheImpl.java:1791)
		at org.infinispan.cache.impl.CacheImpl.put(CacheImpl.java:223)
		at org.infinispan.cache.impl.AbstractDelegatingCache.put(AbstractDelegatingCache.java:449)
		at org.infinispan.cache.impl.EncoderCache.put(EncoderCache.java:711)
		at testing.TestClusterClientBase.performWithProps(TestClusterClientBase.java:67)
		at testing.RelayTest.lambda$main$0(RelayTest.java:42)
		at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
		at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
		... 3 more
Caused by: org.infinispan.util.concurrent.TimeoutException: ISPN000299: Unable to acquire lock after 10 seconds for key test and requestor CommandInvocation:nyc_node_16:763. Lock is held by CommandInvocation:nyc_node_20:758
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:64)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
	at org.infinispan.marshall.exts.ThrowableExternalizer.readGenericThrowable(ThrowableExternalizer.java:282)
	at org.infinispan.marshall.exts.ThrowableExternalizer.readObject(ThrowableExternalizer.java:259)
	at org.infinispan.marshall.exts.ThrowableExternalizer.readObject(ThrowableExternalizer.java:42)
	at org.infinispan.marshall.core.GlobalMarshaller.readWithExternalizer(GlobalMarshaller.java:728)
	at org.infinispan.marshall.core.GlobalMarshaller.readNonNullableObject(GlobalMarshaller.java:709)
	at org.infinispan.marshall.core.GlobalMarshaller.readNullableObject(GlobalMarshaller.java:358)
	at org.infinispan.marshall.core.BytesObjectInput.readObject(BytesObjectInput.java:32)
	at org.infinispan.remoting.responses.ExceptionResponse$Externalizer.readObject(ExceptionResponse.java:49)
	at org.infinispan.remoting.responses.ExceptionResponse$Externalizer.readObject(ExceptionResponse.java:41)
	at org.infinispan.marshall.core.GlobalMarshaller.readWithExternalizer(GlobalMarshaller.java:728)
	at org.infinispan.marshall.core.GlobalMarshaller.readNonNullableObject(GlobalMarshaller.java:709)
	at org.infinispan.marshall.core.GlobalMarshaller.readNullableObject(GlobalMarshaller.java:358)
	at org.infinispan.marshall.core.GlobalMarshaller.objectFromObjectInput(GlobalMarshaller.java:192)
	at org.infinispan.marshall.core.GlobalMarshaller.objectFromByteBuffer(GlobalMarshaller.java:221)
	at org.infinispan.remoting.transport.jgroups.JGroupsTransport.processResponse(JGroupsTransport.java:1394)
	... 26 more
```