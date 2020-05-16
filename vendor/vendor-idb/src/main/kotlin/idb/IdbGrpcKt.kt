package idb

import idb.CompanionServiceGrpc.getServiceDescriptor
import io.grpc.CallOptions
import io.grpc.CallOptions.DEFAULT
import io.grpc.Channel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerServiceDefinition
import io.grpc.ServerServiceDefinition.builder
import io.grpc.ServiceDescriptor
import io.grpc.Status
import io.grpc.Status.UNIMPLEMENTED
import io.grpc.StatusException
import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.kotlin.AbstractCoroutineStub
import io.grpc.kotlin.ClientCalls
import io.grpc.kotlin.ClientCalls.bidiStreamingRpc
import io.grpc.kotlin.ClientCalls.clientStreamingRpc
import io.grpc.kotlin.ClientCalls.serverStreamingRpc
import io.grpc.kotlin.ClientCalls.unaryRpc
import io.grpc.kotlin.ServerCalls
import io.grpc.kotlin.ServerCalls.bidiStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.clientStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.serverStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.unaryServerMethodDefinition
import io.grpc.kotlin.StubFor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

/**
 * Holder for Kotlin coroutine-based client and server APIs for idb.CompanionService.
 */
object CompanionServiceGrpcKt {
  @JvmStatic
  val serviceDescriptor: ServiceDescriptor
    get() = CompanionServiceGrpc.getServiceDescriptor()

  val accessibilityInfoMethod: MethodDescriptor<AccessibilityInfoRequest, AccessibilityInfoResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getAccessibilityInfoMethod()

  val addMediaMethod: MethodDescriptor<AddMediaRequest, AddMediaResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getAddMediaMethod()

  val approveMethod: MethodDescriptor<ApproveRequest, ApproveResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getApproveMethod()

  val bootMethod: MethodDescriptor<BootRequest, BootResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getBootMethod()

  val clearKeychainMethod: MethodDescriptor<ClearKeychainRequest, ClearKeychainResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getClearKeychainMethod()

  val connectMethod: MethodDescriptor<ConnectRequest, ConnectResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getConnectMethod()

  val contactsUpdateMethod: MethodDescriptor<ContactsUpdateRequest, ContactsUpdateResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getContactsUpdateMethod()

  val crashDeleteMethod: MethodDescriptor<CrashLogQuery, CrashLogResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getCrashDeleteMethod()

  val crashListMethod: MethodDescriptor<CrashLogQuery, CrashLogResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getCrashListMethod()

  val crashShowMethod: MethodDescriptor<CrashShowRequest, CrashShowResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getCrashShowMethod()

  val debugserverMethod: MethodDescriptor<DebugServerRequest, DebugServerResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getDebugserverMethod()

  val describeMethod: MethodDescriptor<TargetDescriptionRequest, TargetDescriptionResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getDescribeMethod()

  val disconnectMethod: MethodDescriptor<DisconnectRequest, DisconnectResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getDisconnectMethod()

  val focusMethod: MethodDescriptor<FocusRequest, FocusResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getFocusMethod()

  val hidMethod: MethodDescriptor<HIDEvent, HIDResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getHidMethod()

  val installMethod: MethodDescriptor<InstallRequest, InstallResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getInstallMethod()

  val instrumentsRunMethod: MethodDescriptor<InstrumentsRunRequest, InstrumentsRunResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getInstrumentsRunMethod()

  val launchMethod: MethodDescriptor<LaunchRequest, LaunchResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getLaunchMethod()

  val listAppsMethod: MethodDescriptor<ListAppsRequest, ListAppsResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getListAppsMethod()

  val listTargetsMethod: MethodDescriptor<ListTargetsRequest, ListTargetsResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getListTargetsMethod()

  val logMethod: MethodDescriptor<LogRequest, LogResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getLogMethod()

  val lsMethod: MethodDescriptor<LsRequest, LsResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getLsMethod()

  val mkdirMethod: MethodDescriptor<MkdirRequest, MkdirResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getMkdirMethod()

  val mvMethod: MethodDescriptor<MvRequest, MvResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getMvMethod()

  val openUrlMethod: MethodDescriptor<OpenUrlRequest, OpenUrlRequest>
    @JvmStatic
    get() = CompanionServiceGrpc.getOpenUrlMethod()

  val pullMethod: MethodDescriptor<PullRequest, PullResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getPullMethod()

  val pushMethod: MethodDescriptor<PushRequest, PushResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getPushMethod()

  val recordMethod: MethodDescriptor<RecordRequest, RecordResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getRecordMethod()

  val rmMethod: MethodDescriptor<RmRequest, RmResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getRmMethod()

  val screenshotMethod: MethodDescriptor<ScreenshotRequest, ScreenshotResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getScreenshotMethod()

  val setLocationMethod: MethodDescriptor<SetLocationRequest, SetLocationResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getSetLocationMethod()

  val terminateMethod: MethodDescriptor<TerminateRequest, TerminateResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getTerminateMethod()

  val uninstallMethod: MethodDescriptor<UninstallRequest, UninstallResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getUninstallMethod()

  val xctestListBundlesMethod: MethodDescriptor<XctestListBundlesRequest, XctestListBundlesResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getXctestListBundlesMethod()

  val xctestListTestsMethod: MethodDescriptor<XctestListTestsRequest, XctestListTestsResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getXctestListTestsMethod()

  val xctestRunMethod: MethodDescriptor<XctestRunRequest, XctestRunResponse>
    @JvmStatic
    get() = CompanionServiceGrpc.getXctestRunMethod()

  /**
   * A stub for issuing RPCs to a(n) idb.CompanionService service as suspending coroutines.
   */
  @StubFor(CompanionServiceGrpc::class)
  class CompanionServiceCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT
  ) : AbstractCoroutineStub<CompanionServiceCoroutineStub>(channel, callOptions) {
    override fun build(channel: Channel, callOptions: CallOptions): CompanionServiceCoroutineStub =
        CompanionServiceCoroutineStub(channel, callOptions)

    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun accessibilityInfo(request: AccessibilityInfoRequest): AccessibilityInfoResponse =
        unaryRpc(
      channel,
      CompanionServiceGrpc.getAccessibilityInfoMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * This function collects the [Flow] of requests.  If the server terminates the RPC
     * for any reason before collection of requests is complete, the collection of requests
     * will be cancelled.  If the collection of requests completes exceptionally for any other
     * reason, the RPC will be cancelled for that reason and this method will throw that
     * exception.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return The single response from the server.
     */
    suspend fun addMedia(requests: Flow<AddMediaRequest>): AddMediaResponse = clientStreamingRpc(
      channel,
      CompanionServiceGrpc.getAddMediaMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun approve(request: ApproveRequest): ApproveResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getApproveMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun boot(request: BootRequest): BootResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getBootMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun clearKeychain(request: ClearKeychainRequest): ClearKeychainResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getClearKeychainMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun connect(request: ConnectRequest): ConnectResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getConnectMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun contactsUpdate(request: ContactsUpdateRequest): ContactsUpdateResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getContactsUpdateMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun crashDelete(request: CrashLogQuery): CrashLogResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getCrashDeleteMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun crashList(request: CrashLogQuery): CrashLogResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getCrashListMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun crashShow(request: CrashShowRequest): CrashShowResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getCrashShowMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun debugserver(requests: Flow<DebugServerRequest>): Flow<DebugServerResponse> =
        bidiStreamingRpc(
      channel,
      CompanionServiceGrpc.getDebugserverMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun describe(request: TargetDescriptionRequest): TargetDescriptionResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getDescribeMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun disconnect(request: DisconnectRequest): DisconnectResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getDisconnectMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun focus(request: FocusRequest): FocusResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getFocusMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * This function collects the [Flow] of requests.  If the server terminates the RPC
     * for any reason before collection of requests is complete, the collection of requests
     * will be cancelled.  If the collection of requests completes exceptionally for any other
     * reason, the RPC will be cancelled for that reason and this method will throw that
     * exception.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return The single response from the server.
     */
    suspend fun hid(requests: Flow<HIDEvent>): HIDResponse = clientStreamingRpc(
      channel,
      CompanionServiceGrpc.getHidMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun install(requests: Flow<InstallRequest>): Flow<InstallResponse> = bidiStreamingRpc(
      channel,
      CompanionServiceGrpc.getInstallMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun instrumentsRun(requests: Flow<InstrumentsRunRequest>): Flow<InstrumentsRunResponse> =
        bidiStreamingRpc(
      channel,
      CompanionServiceGrpc.getInstrumentsRunMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun launch(requests: Flow<LaunchRequest>): Flow<LaunchResponse> = bidiStreamingRpc(
      channel,
      CompanionServiceGrpc.getLaunchMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun listApps(request: ListAppsRequest): ListAppsResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getListAppsMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun listTargets(request: ListTargetsRequest): ListTargetsResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getListTargetsMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun log(request: LogRequest): Flow<LogResponse> = serverStreamingRpc(
      channel,
      CompanionServiceGrpc.getLogMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun ls(request: LsRequest): LsResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getLsMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun mkdir(request: MkdirRequest): MkdirResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getMkdirMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun mv(request: MvRequest): MvResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getMvMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun openUrl(request: OpenUrlRequest): OpenUrlRequest = unaryRpc(
      channel,
      CompanionServiceGrpc.getOpenUrlMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun pull(request: PullRequest): Flow<PullResponse> = serverStreamingRpc(
      channel,
      CompanionServiceGrpc.getPullMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * This function collects the [Flow] of requests.  If the server terminates the RPC
     * for any reason before collection of requests is complete, the collection of requests
     * will be cancelled.  If the collection of requests completes exceptionally for any other
     * reason, the RPC will be cancelled for that reason and this method will throw that
     * exception.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return The single response from the server.
     */
    suspend fun push(requests: Flow<PushRequest>): PushResponse = clientStreamingRpc(
      channel,
      CompanionServiceGrpc.getPushMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun record(requests: Flow<RecordRequest>): Flow<RecordResponse> = bidiStreamingRpc(
      channel,
      CompanionServiceGrpc.getRecordMethod(),
      requests,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun rm(request: RmRequest): RmResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getRmMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun screenshot(request: ScreenshotRequest): ScreenshotResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getScreenshotMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun setLocation(request: SetLocationRequest): SetLocationResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getSetLocationMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun terminate(request: TerminateRequest): TerminateResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getTerminateMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun uninstall(request: UninstallRequest): UninstallResponse = unaryRpc(
      channel,
      CompanionServiceGrpc.getUninstallMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun xctestListBundles(request: XctestListBundlesRequest): XctestListBundlesResponse =
        unaryRpc(
      channel,
      CompanionServiceGrpc.getXctestListBundlesMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Executes this RPC and returns the response message, suspending until the RPC completes
     * with [`Status.OK`][Status].  If the RPC completes with another status, a corresponding
     * [StatusException] is thrown.  If this coroutine is cancelled, the RPC is also cancelled
     * with the corresponding exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return The single response from the server.
     */
    suspend fun xctestListTests(request: XctestListTestsRequest): XctestListTestsResponse =
        unaryRpc(
      channel,
      CompanionServiceGrpc.getXctestListTestsMethod(),
      request,
      callOptions,
      Metadata()
    )
    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    fun xctestRun(request: XctestRunRequest): Flow<XctestRunResponse> = serverStreamingRpc(
      channel,
      CompanionServiceGrpc.getXctestRunMethod(),
      request,
      callOptions,
      Metadata()
    )}

  /**
   * Skeletal implementation of the idb.CompanionService service based on Kotlin coroutines.
   */
  abstract class CompanionServiceCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns the response to an RPC for idb.CompanionService.accessibility_info.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun accessibilityInfo(request: AccessibilityInfoRequest): AccessibilityInfoResponse
        = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.accessibility_info is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.add_media.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open suspend fun addMedia(requests: Flow<AddMediaRequest>): AddMediaResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.add_media is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.approve.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun approve(request: ApproveRequest): ApproveResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.approve is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.boot.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun boot(request: BootRequest): BootResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.boot is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.clear_keychain.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun clearKeychain(request: ClearKeychainRequest): ClearKeychainResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.clear_keychain is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.connect.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun connect(request: ConnectRequest): ConnectResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.connect is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.contacts_update.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun contactsUpdate(request: ContactsUpdateRequest): ContactsUpdateResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.contacts_update is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.crash_delete.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun crashDelete(request: CrashLogQuery): CrashLogResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.crash_delete is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.crash_list.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun crashList(request: CrashLogQuery): CrashLogResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.crash_list is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.crash_show.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun crashShow(request: CrashShowRequest): CrashShowResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.crash_show is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.debugserver.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open fun debugserver(requests: Flow<DebugServerRequest>): Flow<DebugServerResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.debugserver is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.describe.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun describe(request: TargetDescriptionRequest): TargetDescriptionResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.describe is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.disconnect.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun disconnect(request: DisconnectRequest): DisconnectResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.disconnect is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.focus.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun focus(request: FocusRequest): FocusResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.focus is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.hid.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open suspend fun hid(requests: Flow<HIDEvent>): HIDResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.hid is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.install.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open fun install(requests: Flow<InstallRequest>): Flow<InstallResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.install is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.instruments_run.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open fun instrumentsRun(requests: Flow<InstrumentsRunRequest>): Flow<InstrumentsRunResponse> =
        throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.instruments_run is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.launch.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open fun launch(requests: Flow<LaunchRequest>): Flow<LaunchResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.launch is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.list_apps.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun listApps(request: ListAppsRequest): ListAppsResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.list_apps is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.list_targets.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun listTargets(request: ListTargetsRequest): ListTargetsResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.list_targets is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.log.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open fun log(request: LogRequest): Flow<LogResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.log is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.ls.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun ls(request: LsRequest): LsResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.ls is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.mkdir.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun mkdir(request: MkdirRequest): MkdirResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.mkdir is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.mv.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun mv(request: MvRequest): MvResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.mv is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.open_url.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun openUrl(request: OpenUrlRequest): OpenUrlRequest = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.open_url is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.pull.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open fun pull(request: PullRequest): Flow<PullResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.pull is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.push.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open suspend fun push(requests: Flow<PushRequest>): PushResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.push is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.record.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    open fun record(requests: Flow<RecordRequest>): Flow<RecordResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.record is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.rm.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun rm(request: RmRequest): RmResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.rm is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.screenshot.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun screenshot(request: ScreenshotRequest): ScreenshotResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.screenshot is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.set_location.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun setLocation(request: SetLocationRequest): SetLocationResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.set_location is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.terminate.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun terminate(request: TerminateRequest): TerminateResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.terminate is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.uninstall.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun uninstall(request: UninstallRequest): UninstallResponse = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.uninstall is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.xctest_list_bundles.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun xctestListBundles(request: XctestListBundlesRequest): XctestListBundlesResponse
        = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.xctest_list_bundles is unimplemented"))

    /**
     * Returns the response to an RPC for idb.CompanionService.xctest_list_tests.
     *
     * If this method fails with a [StatusException], the RPC will fail with the corresponding
     * [Status].  If this method fails with a [java.util.concurrent.CancellationException], the RPC
     * will fail
     * with status `Status.CANCELLED`.  If this method fails for any other reason, the RPC will
     * fail with `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open suspend fun xctestListTests(request: XctestListTestsRequest): XctestListTestsResponse =
        throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.xctest_list_tests is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for idb.CompanionService.xctest_run.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    open fun xctestRun(request: XctestRunRequest): Flow<XctestRunResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method idb.CompanionService.xctest_run is unimplemented"))

    final override fun bindService(): ServerServiceDefinition = builder(getServiceDescriptor())
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getAccessibilityInfoMethod(),
      implementation = ::accessibilityInfo
    ))
      .addMethod(clientStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getAddMediaMethod(),
      implementation = ::addMedia
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getApproveMethod(),
      implementation = ::approve
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getBootMethod(),
      implementation = ::boot
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getClearKeychainMethod(),
      implementation = ::clearKeychain
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getConnectMethod(),
      implementation = ::connect
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getContactsUpdateMethod(),
      implementation = ::contactsUpdate
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getCrashDeleteMethod(),
      implementation = ::crashDelete
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getCrashListMethod(),
      implementation = ::crashList
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getCrashShowMethod(),
      implementation = ::crashShow
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getDebugserverMethod(),
      implementation = ::debugserver
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getDescribeMethod(),
      implementation = ::describe
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getDisconnectMethod(),
      implementation = ::disconnect
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getFocusMethod(),
      implementation = ::focus
    ))
      .addMethod(clientStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getHidMethod(),
      implementation = ::hid
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getInstallMethod(),
      implementation = ::install
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getInstrumentsRunMethod(),
      implementation = ::instrumentsRun
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getLaunchMethod(),
      implementation = ::launch
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getListAppsMethod(),
      implementation = ::listApps
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getListTargetsMethod(),
      implementation = ::listTargets
    ))
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getLogMethod(),
      implementation = ::log
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getLsMethod(),
      implementation = ::ls
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getMkdirMethod(),
      implementation = ::mkdir
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getMvMethod(),
      implementation = ::mv
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getOpenUrlMethod(),
      implementation = ::openUrl
    ))
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getPullMethod(),
      implementation = ::pull
    ))
      .addMethod(clientStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getPushMethod(),
      implementation = ::push
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getRecordMethod(),
      implementation = ::record
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getRmMethod(),
      implementation = ::rm
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getScreenshotMethod(),
      implementation = ::screenshot
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getSetLocationMethod(),
      implementation = ::setLocation
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getTerminateMethod(),
      implementation = ::terminate
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getUninstallMethod(),
      implementation = ::uninstall
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getXctestListBundlesMethod(),
      implementation = ::xctestListBundles
    ))
      .addMethod(unaryServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getXctestListTestsMethod(),
      implementation = ::xctestListTests
    ))
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = CompanionServiceGrpc.getXctestRunMethod(),
      implementation = ::xctestRun
    )).build()
  }
}
