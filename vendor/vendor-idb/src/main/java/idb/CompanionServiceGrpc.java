package idb;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * The idb companion service definition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.1)",
    comments = "Source: idb.proto")
public final class CompanionServiceGrpc {

  private CompanionServiceGrpc() {}

  public static final String SERVICE_NAME = "idb.CompanionService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<idb.AccessibilityInfoRequest,
      idb.AccessibilityInfoResponse> getAccessibilityInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "accessibility_info",
      requestType = idb.AccessibilityInfoRequest.class,
      responseType = idb.AccessibilityInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.AccessibilityInfoRequest,
      idb.AccessibilityInfoResponse> getAccessibilityInfoMethod() {
    io.grpc.MethodDescriptor<idb.AccessibilityInfoRequest, idb.AccessibilityInfoResponse> getAccessibilityInfoMethod;
    if ((getAccessibilityInfoMethod = CompanionServiceGrpc.getAccessibilityInfoMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getAccessibilityInfoMethod = CompanionServiceGrpc.getAccessibilityInfoMethod) == null) {
          CompanionServiceGrpc.getAccessibilityInfoMethod = getAccessibilityInfoMethod = 
              io.grpc.MethodDescriptor.<idb.AccessibilityInfoRequest, idb.AccessibilityInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "accessibility_info"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.AccessibilityInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.AccessibilityInfoResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("accessibility_info"))
                  .build();
          }
        }
     }
     return getAccessibilityInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.AddMediaRequest,
      idb.AddMediaResponse> getAddMediaMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "add_media",
      requestType = idb.AddMediaRequest.class,
      responseType = idb.AddMediaResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<idb.AddMediaRequest,
      idb.AddMediaResponse> getAddMediaMethod() {
    io.grpc.MethodDescriptor<idb.AddMediaRequest, idb.AddMediaResponse> getAddMediaMethod;
    if ((getAddMediaMethod = CompanionServiceGrpc.getAddMediaMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getAddMediaMethod = CompanionServiceGrpc.getAddMediaMethod) == null) {
          CompanionServiceGrpc.getAddMediaMethod = getAddMediaMethod = 
              io.grpc.MethodDescriptor.<idb.AddMediaRequest, idb.AddMediaResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "add_media"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.AddMediaRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.AddMediaResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("add_media"))
                  .build();
          }
        }
     }
     return getAddMediaMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.ApproveRequest,
      idb.ApproveResponse> getApproveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "approve",
      requestType = idb.ApproveRequest.class,
      responseType = idb.ApproveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.ApproveRequest,
      idb.ApproveResponse> getApproveMethod() {
    io.grpc.MethodDescriptor<idb.ApproveRequest, idb.ApproveResponse> getApproveMethod;
    if ((getApproveMethod = CompanionServiceGrpc.getApproveMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getApproveMethod = CompanionServiceGrpc.getApproveMethod) == null) {
          CompanionServiceGrpc.getApproveMethod = getApproveMethod = 
              io.grpc.MethodDescriptor.<idb.ApproveRequest, idb.ApproveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "approve"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ApproveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ApproveResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("approve"))
                  .build();
          }
        }
     }
     return getApproveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.BootRequest,
      idb.BootResponse> getBootMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "boot",
      requestType = idb.BootRequest.class,
      responseType = idb.BootResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.BootRequest,
      idb.BootResponse> getBootMethod() {
    io.grpc.MethodDescriptor<idb.BootRequest, idb.BootResponse> getBootMethod;
    if ((getBootMethod = CompanionServiceGrpc.getBootMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getBootMethod = CompanionServiceGrpc.getBootMethod) == null) {
          CompanionServiceGrpc.getBootMethod = getBootMethod = 
              io.grpc.MethodDescriptor.<idb.BootRequest, idb.BootResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "boot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.BootRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.BootResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("boot"))
                  .build();
          }
        }
     }
     return getBootMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.ClearKeychainRequest,
      idb.ClearKeychainResponse> getClearKeychainMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "clear_keychain",
      requestType = idb.ClearKeychainRequest.class,
      responseType = idb.ClearKeychainResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.ClearKeychainRequest,
      idb.ClearKeychainResponse> getClearKeychainMethod() {
    io.grpc.MethodDescriptor<idb.ClearKeychainRequest, idb.ClearKeychainResponse> getClearKeychainMethod;
    if ((getClearKeychainMethod = CompanionServiceGrpc.getClearKeychainMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getClearKeychainMethod = CompanionServiceGrpc.getClearKeychainMethod) == null) {
          CompanionServiceGrpc.getClearKeychainMethod = getClearKeychainMethod = 
              io.grpc.MethodDescriptor.<idb.ClearKeychainRequest, idb.ClearKeychainResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "clear_keychain"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ClearKeychainRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ClearKeychainResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("clear_keychain"))
                  .build();
          }
        }
     }
     return getClearKeychainMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.ConnectRequest,
      idb.ConnectResponse> getConnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "connect",
      requestType = idb.ConnectRequest.class,
      responseType = idb.ConnectResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.ConnectRequest,
      idb.ConnectResponse> getConnectMethod() {
    io.grpc.MethodDescriptor<idb.ConnectRequest, idb.ConnectResponse> getConnectMethod;
    if ((getConnectMethod = CompanionServiceGrpc.getConnectMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getConnectMethod = CompanionServiceGrpc.getConnectMethod) == null) {
          CompanionServiceGrpc.getConnectMethod = getConnectMethod = 
              io.grpc.MethodDescriptor.<idb.ConnectRequest, idb.ConnectResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "connect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ConnectRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ConnectResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("connect"))
                  .build();
          }
        }
     }
     return getConnectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.ContactsUpdateRequest,
      idb.ContactsUpdateResponse> getContactsUpdateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "contacts_update",
      requestType = idb.ContactsUpdateRequest.class,
      responseType = idb.ContactsUpdateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.ContactsUpdateRequest,
      idb.ContactsUpdateResponse> getContactsUpdateMethod() {
    io.grpc.MethodDescriptor<idb.ContactsUpdateRequest, idb.ContactsUpdateResponse> getContactsUpdateMethod;
    if ((getContactsUpdateMethod = CompanionServiceGrpc.getContactsUpdateMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getContactsUpdateMethod = CompanionServiceGrpc.getContactsUpdateMethod) == null) {
          CompanionServiceGrpc.getContactsUpdateMethod = getContactsUpdateMethod = 
              io.grpc.MethodDescriptor.<idb.ContactsUpdateRequest, idb.ContactsUpdateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "contacts_update"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ContactsUpdateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ContactsUpdateResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("contacts_update"))
                  .build();
          }
        }
     }
     return getContactsUpdateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.CrashLogQuery,
      idb.CrashLogResponse> getCrashDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "crash_delete",
      requestType = idb.CrashLogQuery.class,
      responseType = idb.CrashLogResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.CrashLogQuery,
      idb.CrashLogResponse> getCrashDeleteMethod() {
    io.grpc.MethodDescriptor<idb.CrashLogQuery, idb.CrashLogResponse> getCrashDeleteMethod;
    if ((getCrashDeleteMethod = CompanionServiceGrpc.getCrashDeleteMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getCrashDeleteMethod = CompanionServiceGrpc.getCrashDeleteMethod) == null) {
          CompanionServiceGrpc.getCrashDeleteMethod = getCrashDeleteMethod = 
              io.grpc.MethodDescriptor.<idb.CrashLogQuery, idb.CrashLogResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "crash_delete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.CrashLogQuery.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.CrashLogResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("crash_delete"))
                  .build();
          }
        }
     }
     return getCrashDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.CrashLogQuery,
      idb.CrashLogResponse> getCrashListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "crash_list",
      requestType = idb.CrashLogQuery.class,
      responseType = idb.CrashLogResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.CrashLogQuery,
      idb.CrashLogResponse> getCrashListMethod() {
    io.grpc.MethodDescriptor<idb.CrashLogQuery, idb.CrashLogResponse> getCrashListMethod;
    if ((getCrashListMethod = CompanionServiceGrpc.getCrashListMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getCrashListMethod = CompanionServiceGrpc.getCrashListMethod) == null) {
          CompanionServiceGrpc.getCrashListMethod = getCrashListMethod = 
              io.grpc.MethodDescriptor.<idb.CrashLogQuery, idb.CrashLogResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "crash_list"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.CrashLogQuery.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.CrashLogResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("crash_list"))
                  .build();
          }
        }
     }
     return getCrashListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.CrashShowRequest,
      idb.CrashShowResponse> getCrashShowMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "crash_show",
      requestType = idb.CrashShowRequest.class,
      responseType = idb.CrashShowResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.CrashShowRequest,
      idb.CrashShowResponse> getCrashShowMethod() {
    io.grpc.MethodDescriptor<idb.CrashShowRequest, idb.CrashShowResponse> getCrashShowMethod;
    if ((getCrashShowMethod = CompanionServiceGrpc.getCrashShowMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getCrashShowMethod = CompanionServiceGrpc.getCrashShowMethod) == null) {
          CompanionServiceGrpc.getCrashShowMethod = getCrashShowMethod = 
              io.grpc.MethodDescriptor.<idb.CrashShowRequest, idb.CrashShowResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "crash_show"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.CrashShowRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.CrashShowResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("crash_show"))
                  .build();
          }
        }
     }
     return getCrashShowMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.DebugServerRequest,
      idb.DebugServerResponse> getDebugserverMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "debugserver",
      requestType = idb.DebugServerRequest.class,
      responseType = idb.DebugServerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<idb.DebugServerRequest,
      idb.DebugServerResponse> getDebugserverMethod() {
    io.grpc.MethodDescriptor<idb.DebugServerRequest, idb.DebugServerResponse> getDebugserverMethod;
    if ((getDebugserverMethod = CompanionServiceGrpc.getDebugserverMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getDebugserverMethod = CompanionServiceGrpc.getDebugserverMethod) == null) {
          CompanionServiceGrpc.getDebugserverMethod = getDebugserverMethod = 
              io.grpc.MethodDescriptor.<idb.DebugServerRequest, idb.DebugServerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "debugserver"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.DebugServerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.DebugServerResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("debugserver"))
                  .build();
          }
        }
     }
     return getDebugserverMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.TargetDescriptionRequest,
      idb.TargetDescriptionResponse> getDescribeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "describe",
      requestType = idb.TargetDescriptionRequest.class,
      responseType = idb.TargetDescriptionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.TargetDescriptionRequest,
      idb.TargetDescriptionResponse> getDescribeMethod() {
    io.grpc.MethodDescriptor<idb.TargetDescriptionRequest, idb.TargetDescriptionResponse> getDescribeMethod;
    if ((getDescribeMethod = CompanionServiceGrpc.getDescribeMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getDescribeMethod = CompanionServiceGrpc.getDescribeMethod) == null) {
          CompanionServiceGrpc.getDescribeMethod = getDescribeMethod = 
              io.grpc.MethodDescriptor.<idb.TargetDescriptionRequest, idb.TargetDescriptionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "describe"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.TargetDescriptionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.TargetDescriptionResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("describe"))
                  .build();
          }
        }
     }
     return getDescribeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.DisconnectRequest,
      idb.DisconnectResponse> getDisconnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "disconnect",
      requestType = idb.DisconnectRequest.class,
      responseType = idb.DisconnectResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.DisconnectRequest,
      idb.DisconnectResponse> getDisconnectMethod() {
    io.grpc.MethodDescriptor<idb.DisconnectRequest, idb.DisconnectResponse> getDisconnectMethod;
    if ((getDisconnectMethod = CompanionServiceGrpc.getDisconnectMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getDisconnectMethod = CompanionServiceGrpc.getDisconnectMethod) == null) {
          CompanionServiceGrpc.getDisconnectMethod = getDisconnectMethod = 
              io.grpc.MethodDescriptor.<idb.DisconnectRequest, idb.DisconnectResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "disconnect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.DisconnectRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.DisconnectResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("disconnect"))
                  .build();
          }
        }
     }
     return getDisconnectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.FocusRequest,
      idb.FocusResponse> getFocusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "focus",
      requestType = idb.FocusRequest.class,
      responseType = idb.FocusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.FocusRequest,
      idb.FocusResponse> getFocusMethod() {
    io.grpc.MethodDescriptor<idb.FocusRequest, idb.FocusResponse> getFocusMethod;
    if ((getFocusMethod = CompanionServiceGrpc.getFocusMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getFocusMethod = CompanionServiceGrpc.getFocusMethod) == null) {
          CompanionServiceGrpc.getFocusMethod = getFocusMethod = 
              io.grpc.MethodDescriptor.<idb.FocusRequest, idb.FocusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "focus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.FocusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.FocusResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("focus"))
                  .build();
          }
        }
     }
     return getFocusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.HIDEvent,
      idb.HIDResponse> getHidMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "hid",
      requestType = idb.HIDEvent.class,
      responseType = idb.HIDResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<idb.HIDEvent,
      idb.HIDResponse> getHidMethod() {
    io.grpc.MethodDescriptor<idb.HIDEvent, idb.HIDResponse> getHidMethod;
    if ((getHidMethod = CompanionServiceGrpc.getHidMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getHidMethod = CompanionServiceGrpc.getHidMethod) == null) {
          CompanionServiceGrpc.getHidMethod = getHidMethod = 
              io.grpc.MethodDescriptor.<idb.HIDEvent, idb.HIDResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "hid"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.HIDEvent.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.HIDResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("hid"))
                  .build();
          }
        }
     }
     return getHidMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.InstallRequest,
      idb.InstallResponse> getInstallMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "install",
      requestType = idb.InstallRequest.class,
      responseType = idb.InstallResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<idb.InstallRequest,
      idb.InstallResponse> getInstallMethod() {
    io.grpc.MethodDescriptor<idb.InstallRequest, idb.InstallResponse> getInstallMethod;
    if ((getInstallMethod = CompanionServiceGrpc.getInstallMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getInstallMethod = CompanionServiceGrpc.getInstallMethod) == null) {
          CompanionServiceGrpc.getInstallMethod = getInstallMethod = 
              io.grpc.MethodDescriptor.<idb.InstallRequest, idb.InstallResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "install"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.InstallRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.InstallResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("install"))
                  .build();
          }
        }
     }
     return getInstallMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.InstrumentsRunRequest,
      idb.InstrumentsRunResponse> getInstrumentsRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "instruments_run",
      requestType = idb.InstrumentsRunRequest.class,
      responseType = idb.InstrumentsRunResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<idb.InstrumentsRunRequest,
      idb.InstrumentsRunResponse> getInstrumentsRunMethod() {
    io.grpc.MethodDescriptor<idb.InstrumentsRunRequest, idb.InstrumentsRunResponse> getInstrumentsRunMethod;
    if ((getInstrumentsRunMethod = CompanionServiceGrpc.getInstrumentsRunMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getInstrumentsRunMethod = CompanionServiceGrpc.getInstrumentsRunMethod) == null) {
          CompanionServiceGrpc.getInstrumentsRunMethod = getInstrumentsRunMethod = 
              io.grpc.MethodDescriptor.<idb.InstrumentsRunRequest, idb.InstrumentsRunResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "instruments_run"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.InstrumentsRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.InstrumentsRunResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("instruments_run"))
                  .build();
          }
        }
     }
     return getInstrumentsRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.LaunchRequest,
      idb.LaunchResponse> getLaunchMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "launch",
      requestType = idb.LaunchRequest.class,
      responseType = idb.LaunchResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<idb.LaunchRequest,
      idb.LaunchResponse> getLaunchMethod() {
    io.grpc.MethodDescriptor<idb.LaunchRequest, idb.LaunchResponse> getLaunchMethod;
    if ((getLaunchMethod = CompanionServiceGrpc.getLaunchMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getLaunchMethod = CompanionServiceGrpc.getLaunchMethod) == null) {
          CompanionServiceGrpc.getLaunchMethod = getLaunchMethod = 
              io.grpc.MethodDescriptor.<idb.LaunchRequest, idb.LaunchResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "launch"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.LaunchRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.LaunchResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("launch"))
                  .build();
          }
        }
     }
     return getLaunchMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.ListAppsRequest,
      idb.ListAppsResponse> getListAppsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "list_apps",
      requestType = idb.ListAppsRequest.class,
      responseType = idb.ListAppsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.ListAppsRequest,
      idb.ListAppsResponse> getListAppsMethod() {
    io.grpc.MethodDescriptor<idb.ListAppsRequest, idb.ListAppsResponse> getListAppsMethod;
    if ((getListAppsMethod = CompanionServiceGrpc.getListAppsMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getListAppsMethod = CompanionServiceGrpc.getListAppsMethod) == null) {
          CompanionServiceGrpc.getListAppsMethod = getListAppsMethod = 
              io.grpc.MethodDescriptor.<idb.ListAppsRequest, idb.ListAppsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "list_apps"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ListAppsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ListAppsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("list_apps"))
                  .build();
          }
        }
     }
     return getListAppsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.ListTargetsRequest,
      idb.ListTargetsResponse> getListTargetsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "list_targets",
      requestType = idb.ListTargetsRequest.class,
      responseType = idb.ListTargetsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.ListTargetsRequest,
      idb.ListTargetsResponse> getListTargetsMethod() {
    io.grpc.MethodDescriptor<idb.ListTargetsRequest, idb.ListTargetsResponse> getListTargetsMethod;
    if ((getListTargetsMethod = CompanionServiceGrpc.getListTargetsMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getListTargetsMethod = CompanionServiceGrpc.getListTargetsMethod) == null) {
          CompanionServiceGrpc.getListTargetsMethod = getListTargetsMethod = 
              io.grpc.MethodDescriptor.<idb.ListTargetsRequest, idb.ListTargetsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "list_targets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ListTargetsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ListTargetsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("list_targets"))
                  .build();
          }
        }
     }
     return getListTargetsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.LogRequest,
      idb.LogResponse> getLogMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "log",
      requestType = idb.LogRequest.class,
      responseType = idb.LogResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<idb.LogRequest,
      idb.LogResponse> getLogMethod() {
    io.grpc.MethodDescriptor<idb.LogRequest, idb.LogResponse> getLogMethod;
    if ((getLogMethod = CompanionServiceGrpc.getLogMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getLogMethod = CompanionServiceGrpc.getLogMethod) == null) {
          CompanionServiceGrpc.getLogMethod = getLogMethod = 
              io.grpc.MethodDescriptor.<idb.LogRequest, idb.LogResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "log"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.LogRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.LogResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("log"))
                  .build();
          }
        }
     }
     return getLogMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.LsRequest,
      idb.LsResponse> getLsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ls",
      requestType = idb.LsRequest.class,
      responseType = idb.LsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.LsRequest,
      idb.LsResponse> getLsMethod() {
    io.grpc.MethodDescriptor<idb.LsRequest, idb.LsResponse> getLsMethod;
    if ((getLsMethod = CompanionServiceGrpc.getLsMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getLsMethod = CompanionServiceGrpc.getLsMethod) == null) {
          CompanionServiceGrpc.getLsMethod = getLsMethod = 
              io.grpc.MethodDescriptor.<idb.LsRequest, idb.LsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "ls"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.LsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.LsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("ls"))
                  .build();
          }
        }
     }
     return getLsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.MkdirRequest,
      idb.MkdirResponse> getMkdirMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "mkdir",
      requestType = idb.MkdirRequest.class,
      responseType = idb.MkdirResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.MkdirRequest,
      idb.MkdirResponse> getMkdirMethod() {
    io.grpc.MethodDescriptor<idb.MkdirRequest, idb.MkdirResponse> getMkdirMethod;
    if ((getMkdirMethod = CompanionServiceGrpc.getMkdirMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getMkdirMethod = CompanionServiceGrpc.getMkdirMethod) == null) {
          CompanionServiceGrpc.getMkdirMethod = getMkdirMethod = 
              io.grpc.MethodDescriptor.<idb.MkdirRequest, idb.MkdirResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "mkdir"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.MkdirRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.MkdirResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("mkdir"))
                  .build();
          }
        }
     }
     return getMkdirMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.MvRequest,
      idb.MvResponse> getMvMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "mv",
      requestType = idb.MvRequest.class,
      responseType = idb.MvResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.MvRequest,
      idb.MvResponse> getMvMethod() {
    io.grpc.MethodDescriptor<idb.MvRequest, idb.MvResponse> getMvMethod;
    if ((getMvMethod = CompanionServiceGrpc.getMvMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getMvMethod = CompanionServiceGrpc.getMvMethod) == null) {
          CompanionServiceGrpc.getMvMethod = getMvMethod = 
              io.grpc.MethodDescriptor.<idb.MvRequest, idb.MvResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "mv"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.MvRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.MvResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("mv"))
                  .build();
          }
        }
     }
     return getMvMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.OpenUrlRequest,
      idb.OpenUrlRequest> getOpenUrlMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "open_url",
      requestType = idb.OpenUrlRequest.class,
      responseType = idb.OpenUrlRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.OpenUrlRequest,
      idb.OpenUrlRequest> getOpenUrlMethod() {
    io.grpc.MethodDescriptor<idb.OpenUrlRequest, idb.OpenUrlRequest> getOpenUrlMethod;
    if ((getOpenUrlMethod = CompanionServiceGrpc.getOpenUrlMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getOpenUrlMethod = CompanionServiceGrpc.getOpenUrlMethod) == null) {
          CompanionServiceGrpc.getOpenUrlMethod = getOpenUrlMethod = 
              io.grpc.MethodDescriptor.<idb.OpenUrlRequest, idb.OpenUrlRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "open_url"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.OpenUrlRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.OpenUrlRequest.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("open_url"))
                  .build();
          }
        }
     }
     return getOpenUrlMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.PullRequest,
      idb.PullResponse> getPullMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "pull",
      requestType = idb.PullRequest.class,
      responseType = idb.PullResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<idb.PullRequest,
      idb.PullResponse> getPullMethod() {
    io.grpc.MethodDescriptor<idb.PullRequest, idb.PullResponse> getPullMethod;
    if ((getPullMethod = CompanionServiceGrpc.getPullMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getPullMethod = CompanionServiceGrpc.getPullMethod) == null) {
          CompanionServiceGrpc.getPullMethod = getPullMethod = 
              io.grpc.MethodDescriptor.<idb.PullRequest, idb.PullResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "pull"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.PullRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.PullResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("pull"))
                  .build();
          }
        }
     }
     return getPullMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.PushRequest,
      idb.PushResponse> getPushMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "push",
      requestType = idb.PushRequest.class,
      responseType = idb.PushResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<idb.PushRequest,
      idb.PushResponse> getPushMethod() {
    io.grpc.MethodDescriptor<idb.PushRequest, idb.PushResponse> getPushMethod;
    if ((getPushMethod = CompanionServiceGrpc.getPushMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getPushMethod = CompanionServiceGrpc.getPushMethod) == null) {
          CompanionServiceGrpc.getPushMethod = getPushMethod = 
              io.grpc.MethodDescriptor.<idb.PushRequest, idb.PushResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "push"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.PushRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.PushResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("push"))
                  .build();
          }
        }
     }
     return getPushMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.RecordRequest,
      idb.RecordResponse> getRecordMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "record",
      requestType = idb.RecordRequest.class,
      responseType = idb.RecordResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<idb.RecordRequest,
      idb.RecordResponse> getRecordMethod() {
    io.grpc.MethodDescriptor<idb.RecordRequest, idb.RecordResponse> getRecordMethod;
    if ((getRecordMethod = CompanionServiceGrpc.getRecordMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getRecordMethod = CompanionServiceGrpc.getRecordMethod) == null) {
          CompanionServiceGrpc.getRecordMethod = getRecordMethod = 
              io.grpc.MethodDescriptor.<idb.RecordRequest, idb.RecordResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "record"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.RecordRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.RecordResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("record"))
                  .build();
          }
        }
     }
     return getRecordMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.RmRequest,
      idb.RmResponse> getRmMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "rm",
      requestType = idb.RmRequest.class,
      responseType = idb.RmResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.RmRequest,
      idb.RmResponse> getRmMethod() {
    io.grpc.MethodDescriptor<idb.RmRequest, idb.RmResponse> getRmMethod;
    if ((getRmMethod = CompanionServiceGrpc.getRmMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getRmMethod = CompanionServiceGrpc.getRmMethod) == null) {
          CompanionServiceGrpc.getRmMethod = getRmMethod = 
              io.grpc.MethodDescriptor.<idb.RmRequest, idb.RmResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "rm"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.RmRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.RmResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("rm"))
                  .build();
          }
        }
     }
     return getRmMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.ScreenshotRequest,
      idb.ScreenshotResponse> getScreenshotMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "screenshot",
      requestType = idb.ScreenshotRequest.class,
      responseType = idb.ScreenshotResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.ScreenshotRequest,
      idb.ScreenshotResponse> getScreenshotMethod() {
    io.grpc.MethodDescriptor<idb.ScreenshotRequest, idb.ScreenshotResponse> getScreenshotMethod;
    if ((getScreenshotMethod = CompanionServiceGrpc.getScreenshotMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getScreenshotMethod = CompanionServiceGrpc.getScreenshotMethod) == null) {
          CompanionServiceGrpc.getScreenshotMethod = getScreenshotMethod = 
              io.grpc.MethodDescriptor.<idb.ScreenshotRequest, idb.ScreenshotResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "screenshot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ScreenshotRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.ScreenshotResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("screenshot"))
                  .build();
          }
        }
     }
     return getScreenshotMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.SetLocationRequest,
      idb.SetLocationResponse> getSetLocationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "set_location",
      requestType = idb.SetLocationRequest.class,
      responseType = idb.SetLocationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.SetLocationRequest,
      idb.SetLocationResponse> getSetLocationMethod() {
    io.grpc.MethodDescriptor<idb.SetLocationRequest, idb.SetLocationResponse> getSetLocationMethod;
    if ((getSetLocationMethod = CompanionServiceGrpc.getSetLocationMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getSetLocationMethod = CompanionServiceGrpc.getSetLocationMethod) == null) {
          CompanionServiceGrpc.getSetLocationMethod = getSetLocationMethod = 
              io.grpc.MethodDescriptor.<idb.SetLocationRequest, idb.SetLocationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "set_location"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.SetLocationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.SetLocationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("set_location"))
                  .build();
          }
        }
     }
     return getSetLocationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.TerminateRequest,
      idb.TerminateResponse> getTerminateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "terminate",
      requestType = idb.TerminateRequest.class,
      responseType = idb.TerminateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.TerminateRequest,
      idb.TerminateResponse> getTerminateMethod() {
    io.grpc.MethodDescriptor<idb.TerminateRequest, idb.TerminateResponse> getTerminateMethod;
    if ((getTerminateMethod = CompanionServiceGrpc.getTerminateMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getTerminateMethod = CompanionServiceGrpc.getTerminateMethod) == null) {
          CompanionServiceGrpc.getTerminateMethod = getTerminateMethod = 
              io.grpc.MethodDescriptor.<idb.TerminateRequest, idb.TerminateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "terminate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.TerminateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.TerminateResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("terminate"))
                  .build();
          }
        }
     }
     return getTerminateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.UninstallRequest,
      idb.UninstallResponse> getUninstallMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "uninstall",
      requestType = idb.UninstallRequest.class,
      responseType = idb.UninstallResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.UninstallRequest,
      idb.UninstallResponse> getUninstallMethod() {
    io.grpc.MethodDescriptor<idb.UninstallRequest, idb.UninstallResponse> getUninstallMethod;
    if ((getUninstallMethod = CompanionServiceGrpc.getUninstallMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getUninstallMethod = CompanionServiceGrpc.getUninstallMethod) == null) {
          CompanionServiceGrpc.getUninstallMethod = getUninstallMethod = 
              io.grpc.MethodDescriptor.<idb.UninstallRequest, idb.UninstallResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "uninstall"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.UninstallRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.UninstallResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("uninstall"))
                  .build();
          }
        }
     }
     return getUninstallMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.XctestListBundlesRequest,
      idb.XctestListBundlesResponse> getXctestListBundlesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "xctest_list_bundles",
      requestType = idb.XctestListBundlesRequest.class,
      responseType = idb.XctestListBundlesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.XctestListBundlesRequest,
      idb.XctestListBundlesResponse> getXctestListBundlesMethod() {
    io.grpc.MethodDescriptor<idb.XctestListBundlesRequest, idb.XctestListBundlesResponse> getXctestListBundlesMethod;
    if ((getXctestListBundlesMethod = CompanionServiceGrpc.getXctestListBundlesMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getXctestListBundlesMethod = CompanionServiceGrpc.getXctestListBundlesMethod) == null) {
          CompanionServiceGrpc.getXctestListBundlesMethod = getXctestListBundlesMethod = 
              io.grpc.MethodDescriptor.<idb.XctestListBundlesRequest, idb.XctestListBundlesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "xctest_list_bundles"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.XctestListBundlesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.XctestListBundlesResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("xctest_list_bundles"))
                  .build();
          }
        }
     }
     return getXctestListBundlesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.XctestListTestsRequest,
      idb.XctestListTestsResponse> getXctestListTestsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "xctest_list_tests",
      requestType = idb.XctestListTestsRequest.class,
      responseType = idb.XctestListTestsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<idb.XctestListTestsRequest,
      idb.XctestListTestsResponse> getXctestListTestsMethod() {
    io.grpc.MethodDescriptor<idb.XctestListTestsRequest, idb.XctestListTestsResponse> getXctestListTestsMethod;
    if ((getXctestListTestsMethod = CompanionServiceGrpc.getXctestListTestsMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getXctestListTestsMethod = CompanionServiceGrpc.getXctestListTestsMethod) == null) {
          CompanionServiceGrpc.getXctestListTestsMethod = getXctestListTestsMethod = 
              io.grpc.MethodDescriptor.<idb.XctestListTestsRequest, idb.XctestListTestsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "xctest_list_tests"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.XctestListTestsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.XctestListTestsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("xctest_list_tests"))
                  .build();
          }
        }
     }
     return getXctestListTestsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<idb.XctestRunRequest,
      idb.XctestRunResponse> getXctestRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "xctest_run",
      requestType = idb.XctestRunRequest.class,
      responseType = idb.XctestRunResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<idb.XctestRunRequest,
      idb.XctestRunResponse> getXctestRunMethod() {
    io.grpc.MethodDescriptor<idb.XctestRunRequest, idb.XctestRunResponse> getXctestRunMethod;
    if ((getXctestRunMethod = CompanionServiceGrpc.getXctestRunMethod) == null) {
      synchronized (CompanionServiceGrpc.class) {
        if ((getXctestRunMethod = CompanionServiceGrpc.getXctestRunMethod) == null) {
          CompanionServiceGrpc.getXctestRunMethod = getXctestRunMethod = 
              io.grpc.MethodDescriptor.<idb.XctestRunRequest, idb.XctestRunResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "idb.CompanionService", "xctest_run"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.XctestRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  idb.XctestRunResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new CompanionServiceMethodDescriptorSupplier("xctest_run"))
                  .build();
          }
        }
     }
     return getXctestRunMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CompanionServiceStub newStub(io.grpc.Channel channel) {
    return new CompanionServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CompanionServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CompanionServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CompanionServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CompanionServiceFutureStub(channel);
  }

  /**
   * <pre>
   * The idb companion service definition.
   * </pre>
   */
  public static abstract class CompanionServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void accessibilityInfo(idb.AccessibilityInfoRequest request,
        io.grpc.stub.StreamObserver<idb.AccessibilityInfoResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAccessibilityInfoMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.AddMediaRequest> addMedia(
        io.grpc.stub.StreamObserver<idb.AddMediaResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getAddMediaMethod(), responseObserver);
    }

    /**
     */
    public void approve(idb.ApproveRequest request,
        io.grpc.stub.StreamObserver<idb.ApproveResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getApproveMethod(), responseObserver);
    }

    /**
     */
    public void boot(idb.BootRequest request,
        io.grpc.stub.StreamObserver<idb.BootResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getBootMethod(), responseObserver);
    }

    /**
     */
    public void clearKeychain(idb.ClearKeychainRequest request,
        io.grpc.stub.StreamObserver<idb.ClearKeychainResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getClearKeychainMethod(), responseObserver);
    }

    /**
     */
    public void connect(idb.ConnectRequest request,
        io.grpc.stub.StreamObserver<idb.ConnectResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getConnectMethod(), responseObserver);
    }

    /**
     */
    public void contactsUpdate(idb.ContactsUpdateRequest request,
        io.grpc.stub.StreamObserver<idb.ContactsUpdateResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getContactsUpdateMethod(), responseObserver);
    }

    /**
     */
    public void crashDelete(idb.CrashLogQuery request,
        io.grpc.stub.StreamObserver<idb.CrashLogResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCrashDeleteMethod(), responseObserver);
    }

    /**
     */
    public void crashList(idb.CrashLogQuery request,
        io.grpc.stub.StreamObserver<idb.CrashLogResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCrashListMethod(), responseObserver);
    }

    /**
     */
    public void crashShow(idb.CrashShowRequest request,
        io.grpc.stub.StreamObserver<idb.CrashShowResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCrashShowMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.DebugServerRequest> debugserver(
        io.grpc.stub.StreamObserver<idb.DebugServerResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getDebugserverMethod(), responseObserver);
    }

    /**
     */
    public void describe(idb.TargetDescriptionRequest request,
        io.grpc.stub.StreamObserver<idb.TargetDescriptionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDescribeMethod(), responseObserver);
    }

    /**
     */
    public void disconnect(idb.DisconnectRequest request,
        io.grpc.stub.StreamObserver<idb.DisconnectResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDisconnectMethod(), responseObserver);
    }

    /**
     */
    public void focus(idb.FocusRequest request,
        io.grpc.stub.StreamObserver<idb.FocusResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFocusMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.HIDEvent> hid(
        io.grpc.stub.StreamObserver<idb.HIDResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getHidMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.InstallRequest> install(
        io.grpc.stub.StreamObserver<idb.InstallResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getInstallMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.InstrumentsRunRequest> instrumentsRun(
        io.grpc.stub.StreamObserver<idb.InstrumentsRunResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getInstrumentsRunMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.LaunchRequest> launch(
        io.grpc.stub.StreamObserver<idb.LaunchResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getLaunchMethod(), responseObserver);
    }

    /**
     */
    public void listApps(idb.ListAppsRequest request,
        io.grpc.stub.StreamObserver<idb.ListAppsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListAppsMethod(), responseObserver);
    }

    /**
     */
    public void listTargets(idb.ListTargetsRequest request,
        io.grpc.stub.StreamObserver<idb.ListTargetsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListTargetsMethod(), responseObserver);
    }

    /**
     */
    public void log(idb.LogRequest request,
        io.grpc.stub.StreamObserver<idb.LogResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getLogMethod(), responseObserver);
    }

    /**
     */
    public void ls(idb.LsRequest request,
        io.grpc.stub.StreamObserver<idb.LsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getLsMethod(), responseObserver);
    }

    /**
     */
    public void mkdir(idb.MkdirRequest request,
        io.grpc.stub.StreamObserver<idb.MkdirResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMkdirMethod(), responseObserver);
    }

    /**
     */
    public void mv(idb.MvRequest request,
        io.grpc.stub.StreamObserver<idb.MvResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMvMethod(), responseObserver);
    }

    /**
     */
    public void openUrl(idb.OpenUrlRequest request,
        io.grpc.stub.StreamObserver<idb.OpenUrlRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getOpenUrlMethod(), responseObserver);
    }

    /**
     */
    public void pull(idb.PullRequest request,
        io.grpc.stub.StreamObserver<idb.PullResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPullMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.PushRequest> push(
        io.grpc.stub.StreamObserver<idb.PushResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getPushMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.RecordRequest> record(
        io.grpc.stub.StreamObserver<idb.RecordResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getRecordMethod(), responseObserver);
    }

    /**
     */
    public void rm(idb.RmRequest request,
        io.grpc.stub.StreamObserver<idb.RmResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRmMethod(), responseObserver);
    }

    /**
     */
    public void screenshot(idb.ScreenshotRequest request,
        io.grpc.stub.StreamObserver<idb.ScreenshotResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getScreenshotMethod(), responseObserver);
    }

    /**
     */
    public void setLocation(idb.SetLocationRequest request,
        io.grpc.stub.StreamObserver<idb.SetLocationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSetLocationMethod(), responseObserver);
    }

    /**
     */
    public void terminate(idb.TerminateRequest request,
        io.grpc.stub.StreamObserver<idb.TerminateResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getTerminateMethod(), responseObserver);
    }

    /**
     */
    public void uninstall(idb.UninstallRequest request,
        io.grpc.stub.StreamObserver<idb.UninstallResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUninstallMethod(), responseObserver);
    }

    /**
     */
    public void xctestListBundles(idb.XctestListBundlesRequest request,
        io.grpc.stub.StreamObserver<idb.XctestListBundlesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getXctestListBundlesMethod(), responseObserver);
    }

    /**
     */
    public void xctestListTests(idb.XctestListTestsRequest request,
        io.grpc.stub.StreamObserver<idb.XctestListTestsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getXctestListTestsMethod(), responseObserver);
    }

    /**
     */
    public void xctestRun(idb.XctestRunRequest request,
        io.grpc.stub.StreamObserver<idb.XctestRunResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getXctestRunMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getAccessibilityInfoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.AccessibilityInfoRequest,
                idb.AccessibilityInfoResponse>(
                  this, METHODID_ACCESSIBILITY_INFO)))
          .addMethod(
            getAddMediaMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                idb.AddMediaRequest,
                idb.AddMediaResponse>(
                  this, METHODID_ADD_MEDIA)))
          .addMethod(
            getApproveMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.ApproveRequest,
                idb.ApproveResponse>(
                  this, METHODID_APPROVE)))
          .addMethod(
            getBootMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.BootRequest,
                idb.BootResponse>(
                  this, METHODID_BOOT)))
          .addMethod(
            getClearKeychainMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.ClearKeychainRequest,
                idb.ClearKeychainResponse>(
                  this, METHODID_CLEAR_KEYCHAIN)))
          .addMethod(
            getConnectMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.ConnectRequest,
                idb.ConnectResponse>(
                  this, METHODID_CONNECT)))
          .addMethod(
            getContactsUpdateMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.ContactsUpdateRequest,
                idb.ContactsUpdateResponse>(
                  this, METHODID_CONTACTS_UPDATE)))
          .addMethod(
            getCrashDeleteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.CrashLogQuery,
                idb.CrashLogResponse>(
                  this, METHODID_CRASH_DELETE)))
          .addMethod(
            getCrashListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.CrashLogQuery,
                idb.CrashLogResponse>(
                  this, METHODID_CRASH_LIST)))
          .addMethod(
            getCrashShowMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.CrashShowRequest,
                idb.CrashShowResponse>(
                  this, METHODID_CRASH_SHOW)))
          .addMethod(
            getDebugserverMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                idb.DebugServerRequest,
                idb.DebugServerResponse>(
                  this, METHODID_DEBUGSERVER)))
          .addMethod(
            getDescribeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.TargetDescriptionRequest,
                idb.TargetDescriptionResponse>(
                  this, METHODID_DESCRIBE)))
          .addMethod(
            getDisconnectMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.DisconnectRequest,
                idb.DisconnectResponse>(
                  this, METHODID_DISCONNECT)))
          .addMethod(
            getFocusMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.FocusRequest,
                idb.FocusResponse>(
                  this, METHODID_FOCUS)))
          .addMethod(
            getHidMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                idb.HIDEvent,
                idb.HIDResponse>(
                  this, METHODID_HID)))
          .addMethod(
            getInstallMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                idb.InstallRequest,
                idb.InstallResponse>(
                  this, METHODID_INSTALL)))
          .addMethod(
            getInstrumentsRunMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                idb.InstrumentsRunRequest,
                idb.InstrumentsRunResponse>(
                  this, METHODID_INSTRUMENTS_RUN)))
          .addMethod(
            getLaunchMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                idb.LaunchRequest,
                idb.LaunchResponse>(
                  this, METHODID_LAUNCH)))
          .addMethod(
            getListAppsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.ListAppsRequest,
                idb.ListAppsResponse>(
                  this, METHODID_LIST_APPS)))
          .addMethod(
            getListTargetsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.ListTargetsRequest,
                idb.ListTargetsResponse>(
                  this, METHODID_LIST_TARGETS)))
          .addMethod(
            getLogMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                idb.LogRequest,
                idb.LogResponse>(
                  this, METHODID_LOG)))
          .addMethod(
            getLsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.LsRequest,
                idb.LsResponse>(
                  this, METHODID_LS)))
          .addMethod(
            getMkdirMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.MkdirRequest,
                idb.MkdirResponse>(
                  this, METHODID_MKDIR)))
          .addMethod(
            getMvMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.MvRequest,
                idb.MvResponse>(
                  this, METHODID_MV)))
          .addMethod(
            getOpenUrlMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.OpenUrlRequest,
                idb.OpenUrlRequest>(
                  this, METHODID_OPEN_URL)))
          .addMethod(
            getPullMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                idb.PullRequest,
                idb.PullResponse>(
                  this, METHODID_PULL)))
          .addMethod(
            getPushMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                idb.PushRequest,
                idb.PushResponse>(
                  this, METHODID_PUSH)))
          .addMethod(
            getRecordMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                idb.RecordRequest,
                idb.RecordResponse>(
                  this, METHODID_RECORD)))
          .addMethod(
            getRmMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.RmRequest,
                idb.RmResponse>(
                  this, METHODID_RM)))
          .addMethod(
            getScreenshotMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.ScreenshotRequest,
                idb.ScreenshotResponse>(
                  this, METHODID_SCREENSHOT)))
          .addMethod(
            getSetLocationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.SetLocationRequest,
                idb.SetLocationResponse>(
                  this, METHODID_SET_LOCATION)))
          .addMethod(
            getTerminateMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.TerminateRequest,
                idb.TerminateResponse>(
                  this, METHODID_TERMINATE)))
          .addMethod(
            getUninstallMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.UninstallRequest,
                idb.UninstallResponse>(
                  this, METHODID_UNINSTALL)))
          .addMethod(
            getXctestListBundlesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.XctestListBundlesRequest,
                idb.XctestListBundlesResponse>(
                  this, METHODID_XCTEST_LIST_BUNDLES)))
          .addMethod(
            getXctestListTestsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                idb.XctestListTestsRequest,
                idb.XctestListTestsResponse>(
                  this, METHODID_XCTEST_LIST_TESTS)))
          .addMethod(
            getXctestRunMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                idb.XctestRunRequest,
                idb.XctestRunResponse>(
                  this, METHODID_XCTEST_RUN)))
          .build();
    }
  }

  /**
   * <pre>
   * The idb companion service definition.
   * </pre>
   */
  public static final class CompanionServiceStub extends io.grpc.stub.AbstractStub<CompanionServiceStub> {
    private CompanionServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CompanionServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CompanionServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CompanionServiceStub(channel, callOptions);
    }

    /**
     */
    public void accessibilityInfo(idb.AccessibilityInfoRequest request,
        io.grpc.stub.StreamObserver<idb.AccessibilityInfoResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAccessibilityInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.AddMediaRequest> addMedia(
        io.grpc.stub.StreamObserver<idb.AddMediaResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getAddMediaMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void approve(idb.ApproveRequest request,
        io.grpc.stub.StreamObserver<idb.ApproveResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getApproveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void boot(idb.BootRequest request,
        io.grpc.stub.StreamObserver<idb.BootResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getBootMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void clearKeychain(idb.ClearKeychainRequest request,
        io.grpc.stub.StreamObserver<idb.ClearKeychainResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getClearKeychainMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void connect(idb.ConnectRequest request,
        io.grpc.stub.StreamObserver<idb.ConnectResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getConnectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void contactsUpdate(idb.ContactsUpdateRequest request,
        io.grpc.stub.StreamObserver<idb.ContactsUpdateResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getContactsUpdateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void crashDelete(idb.CrashLogQuery request,
        io.grpc.stub.StreamObserver<idb.CrashLogResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCrashDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void crashList(idb.CrashLogQuery request,
        io.grpc.stub.StreamObserver<idb.CrashLogResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCrashListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void crashShow(idb.CrashShowRequest request,
        io.grpc.stub.StreamObserver<idb.CrashShowResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCrashShowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.DebugServerRequest> debugserver(
        io.grpc.stub.StreamObserver<idb.DebugServerResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getDebugserverMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void describe(idb.TargetDescriptionRequest request,
        io.grpc.stub.StreamObserver<idb.TargetDescriptionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDescribeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void disconnect(idb.DisconnectRequest request,
        io.grpc.stub.StreamObserver<idb.DisconnectResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDisconnectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void focus(idb.FocusRequest request,
        io.grpc.stub.StreamObserver<idb.FocusResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFocusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.HIDEvent> hid(
        io.grpc.stub.StreamObserver<idb.HIDResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getHidMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.InstallRequest> install(
        io.grpc.stub.StreamObserver<idb.InstallResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getInstallMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.InstrumentsRunRequest> instrumentsRun(
        io.grpc.stub.StreamObserver<idb.InstrumentsRunResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getInstrumentsRunMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.LaunchRequest> launch(
        io.grpc.stub.StreamObserver<idb.LaunchResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getLaunchMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void listApps(idb.ListAppsRequest request,
        io.grpc.stub.StreamObserver<idb.ListAppsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListAppsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listTargets(idb.ListTargetsRequest request,
        io.grpc.stub.StreamObserver<idb.ListTargetsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListTargetsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void log(idb.LogRequest request,
        io.grpc.stub.StreamObserver<idb.LogResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getLogMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void ls(idb.LsRequest request,
        io.grpc.stub.StreamObserver<idb.LsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getLsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void mkdir(idb.MkdirRequest request,
        io.grpc.stub.StreamObserver<idb.MkdirResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMkdirMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void mv(idb.MvRequest request,
        io.grpc.stub.StreamObserver<idb.MvResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMvMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void openUrl(idb.OpenUrlRequest request,
        io.grpc.stub.StreamObserver<idb.OpenUrlRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getOpenUrlMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void pull(idb.PullRequest request,
        io.grpc.stub.StreamObserver<idb.PullResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getPullMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.PushRequest> push(
        io.grpc.stub.StreamObserver<idb.PushResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getPushMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<idb.RecordRequest> record(
        io.grpc.stub.StreamObserver<idb.RecordResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getRecordMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void rm(idb.RmRequest request,
        io.grpc.stub.StreamObserver<idb.RmResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRmMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void screenshot(idb.ScreenshotRequest request,
        io.grpc.stub.StreamObserver<idb.ScreenshotResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getScreenshotMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setLocation(idb.SetLocationRequest request,
        io.grpc.stub.StreamObserver<idb.SetLocationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSetLocationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void terminate(idb.TerminateRequest request,
        io.grpc.stub.StreamObserver<idb.TerminateResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getTerminateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void uninstall(idb.UninstallRequest request,
        io.grpc.stub.StreamObserver<idb.UninstallResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUninstallMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void xctestListBundles(idb.XctestListBundlesRequest request,
        io.grpc.stub.StreamObserver<idb.XctestListBundlesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getXctestListBundlesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void xctestListTests(idb.XctestListTestsRequest request,
        io.grpc.stub.StreamObserver<idb.XctestListTestsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getXctestListTestsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void xctestRun(idb.XctestRunRequest request,
        io.grpc.stub.StreamObserver<idb.XctestRunResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getXctestRunMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The idb companion service definition.
   * </pre>
   */
  public static final class CompanionServiceBlockingStub extends io.grpc.stub.AbstractStub<CompanionServiceBlockingStub> {
    private CompanionServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CompanionServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CompanionServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CompanionServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public idb.AccessibilityInfoResponse accessibilityInfo(idb.AccessibilityInfoRequest request) {
      return blockingUnaryCall(
          getChannel(), getAccessibilityInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.ApproveResponse approve(idb.ApproveRequest request) {
      return blockingUnaryCall(
          getChannel(), getApproveMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.BootResponse boot(idb.BootRequest request) {
      return blockingUnaryCall(
          getChannel(), getBootMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.ClearKeychainResponse clearKeychain(idb.ClearKeychainRequest request) {
      return blockingUnaryCall(
          getChannel(), getClearKeychainMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.ConnectResponse connect(idb.ConnectRequest request) {
      return blockingUnaryCall(
          getChannel(), getConnectMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.ContactsUpdateResponse contactsUpdate(idb.ContactsUpdateRequest request) {
      return blockingUnaryCall(
          getChannel(), getContactsUpdateMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.CrashLogResponse crashDelete(idb.CrashLogQuery request) {
      return blockingUnaryCall(
          getChannel(), getCrashDeleteMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.CrashLogResponse crashList(idb.CrashLogQuery request) {
      return blockingUnaryCall(
          getChannel(), getCrashListMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.CrashShowResponse crashShow(idb.CrashShowRequest request) {
      return blockingUnaryCall(
          getChannel(), getCrashShowMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.TargetDescriptionResponse describe(idb.TargetDescriptionRequest request) {
      return blockingUnaryCall(
          getChannel(), getDescribeMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.DisconnectResponse disconnect(idb.DisconnectRequest request) {
      return blockingUnaryCall(
          getChannel(), getDisconnectMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.FocusResponse focus(idb.FocusRequest request) {
      return blockingUnaryCall(
          getChannel(), getFocusMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.ListAppsResponse listApps(idb.ListAppsRequest request) {
      return blockingUnaryCall(
          getChannel(), getListAppsMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.ListTargetsResponse listTargets(idb.ListTargetsRequest request) {
      return blockingUnaryCall(
          getChannel(), getListTargetsMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<idb.LogResponse> log(
        idb.LogRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getLogMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.LsResponse ls(idb.LsRequest request) {
      return blockingUnaryCall(
          getChannel(), getLsMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.MkdirResponse mkdir(idb.MkdirRequest request) {
      return blockingUnaryCall(
          getChannel(), getMkdirMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.MvResponse mv(idb.MvRequest request) {
      return blockingUnaryCall(
          getChannel(), getMvMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.OpenUrlRequest openUrl(idb.OpenUrlRequest request) {
      return blockingUnaryCall(
          getChannel(), getOpenUrlMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<idb.PullResponse> pull(
        idb.PullRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getPullMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.RmResponse rm(idb.RmRequest request) {
      return blockingUnaryCall(
          getChannel(), getRmMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.ScreenshotResponse screenshot(idb.ScreenshotRequest request) {
      return blockingUnaryCall(
          getChannel(), getScreenshotMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.SetLocationResponse setLocation(idb.SetLocationRequest request) {
      return blockingUnaryCall(
          getChannel(), getSetLocationMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.TerminateResponse terminate(idb.TerminateRequest request) {
      return blockingUnaryCall(
          getChannel(), getTerminateMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.UninstallResponse uninstall(idb.UninstallRequest request) {
      return blockingUnaryCall(
          getChannel(), getUninstallMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.XctestListBundlesResponse xctestListBundles(idb.XctestListBundlesRequest request) {
      return blockingUnaryCall(
          getChannel(), getXctestListBundlesMethod(), getCallOptions(), request);
    }

    /**
     */
    public idb.XctestListTestsResponse xctestListTests(idb.XctestListTestsRequest request) {
      return blockingUnaryCall(
          getChannel(), getXctestListTestsMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<idb.XctestRunResponse> xctestRun(
        idb.XctestRunRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getXctestRunMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The idb companion service definition.
   * </pre>
   */
  public static final class CompanionServiceFutureStub extends io.grpc.stub.AbstractStub<CompanionServiceFutureStub> {
    private CompanionServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CompanionServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CompanionServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CompanionServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.AccessibilityInfoResponse> accessibilityInfo(
        idb.AccessibilityInfoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAccessibilityInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.ApproveResponse> approve(
        idb.ApproveRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getApproveMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.BootResponse> boot(
        idb.BootRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getBootMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.ClearKeychainResponse> clearKeychain(
        idb.ClearKeychainRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getClearKeychainMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.ConnectResponse> connect(
        idb.ConnectRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getConnectMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.ContactsUpdateResponse> contactsUpdate(
        idb.ContactsUpdateRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getContactsUpdateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.CrashLogResponse> crashDelete(
        idb.CrashLogQuery request) {
      return futureUnaryCall(
          getChannel().newCall(getCrashDeleteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.CrashLogResponse> crashList(
        idb.CrashLogQuery request) {
      return futureUnaryCall(
          getChannel().newCall(getCrashListMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.CrashShowResponse> crashShow(
        idb.CrashShowRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCrashShowMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.TargetDescriptionResponse> describe(
        idb.TargetDescriptionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDescribeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.DisconnectResponse> disconnect(
        idb.DisconnectRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDisconnectMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.FocusResponse> focus(
        idb.FocusRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFocusMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.ListAppsResponse> listApps(
        idb.ListAppsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListAppsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.ListTargetsResponse> listTargets(
        idb.ListTargetsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListTargetsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.LsResponse> ls(
        idb.LsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getLsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.MkdirResponse> mkdir(
        idb.MkdirRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMkdirMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.MvResponse> mv(
        idb.MvRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMvMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.OpenUrlRequest> openUrl(
        idb.OpenUrlRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getOpenUrlMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.RmResponse> rm(
        idb.RmRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRmMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.ScreenshotResponse> screenshot(
        idb.ScreenshotRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getScreenshotMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.SetLocationResponse> setLocation(
        idb.SetLocationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSetLocationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.TerminateResponse> terminate(
        idb.TerminateRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getTerminateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.UninstallResponse> uninstall(
        idb.UninstallRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUninstallMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.XctestListBundlesResponse> xctestListBundles(
        idb.XctestListBundlesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getXctestListBundlesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<idb.XctestListTestsResponse> xctestListTests(
        idb.XctestListTestsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getXctestListTestsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ACCESSIBILITY_INFO = 0;
  private static final int METHODID_APPROVE = 1;
  private static final int METHODID_BOOT = 2;
  private static final int METHODID_CLEAR_KEYCHAIN = 3;
  private static final int METHODID_CONNECT = 4;
  private static final int METHODID_CONTACTS_UPDATE = 5;
  private static final int METHODID_CRASH_DELETE = 6;
  private static final int METHODID_CRASH_LIST = 7;
  private static final int METHODID_CRASH_SHOW = 8;
  private static final int METHODID_DESCRIBE = 9;
  private static final int METHODID_DISCONNECT = 10;
  private static final int METHODID_FOCUS = 11;
  private static final int METHODID_LIST_APPS = 12;
  private static final int METHODID_LIST_TARGETS = 13;
  private static final int METHODID_LOG = 14;
  private static final int METHODID_LS = 15;
  private static final int METHODID_MKDIR = 16;
  private static final int METHODID_MV = 17;
  private static final int METHODID_OPEN_URL = 18;
  private static final int METHODID_PULL = 19;
  private static final int METHODID_RM = 20;
  private static final int METHODID_SCREENSHOT = 21;
  private static final int METHODID_SET_LOCATION = 22;
  private static final int METHODID_TERMINATE = 23;
  private static final int METHODID_UNINSTALL = 24;
  private static final int METHODID_XCTEST_LIST_BUNDLES = 25;
  private static final int METHODID_XCTEST_LIST_TESTS = 26;
  private static final int METHODID_XCTEST_RUN = 27;
  private static final int METHODID_ADD_MEDIA = 28;
  private static final int METHODID_DEBUGSERVER = 29;
  private static final int METHODID_HID = 30;
  private static final int METHODID_INSTALL = 31;
  private static final int METHODID_INSTRUMENTS_RUN = 32;
  private static final int METHODID_LAUNCH = 33;
  private static final int METHODID_PUSH = 34;
  private static final int METHODID_RECORD = 35;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CompanionServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CompanionServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ACCESSIBILITY_INFO:
          serviceImpl.accessibilityInfo((idb.AccessibilityInfoRequest) request,
              (io.grpc.stub.StreamObserver<idb.AccessibilityInfoResponse>) responseObserver);
          break;
        case METHODID_APPROVE:
          serviceImpl.approve((idb.ApproveRequest) request,
              (io.grpc.stub.StreamObserver<idb.ApproveResponse>) responseObserver);
          break;
        case METHODID_BOOT:
          serviceImpl.boot((idb.BootRequest) request,
              (io.grpc.stub.StreamObserver<idb.BootResponse>) responseObserver);
          break;
        case METHODID_CLEAR_KEYCHAIN:
          serviceImpl.clearKeychain((idb.ClearKeychainRequest) request,
              (io.grpc.stub.StreamObserver<idb.ClearKeychainResponse>) responseObserver);
          break;
        case METHODID_CONNECT:
          serviceImpl.connect((idb.ConnectRequest) request,
              (io.grpc.stub.StreamObserver<idb.ConnectResponse>) responseObserver);
          break;
        case METHODID_CONTACTS_UPDATE:
          serviceImpl.contactsUpdate((idb.ContactsUpdateRequest) request,
              (io.grpc.stub.StreamObserver<idb.ContactsUpdateResponse>) responseObserver);
          break;
        case METHODID_CRASH_DELETE:
          serviceImpl.crashDelete((idb.CrashLogQuery) request,
              (io.grpc.stub.StreamObserver<idb.CrashLogResponse>) responseObserver);
          break;
        case METHODID_CRASH_LIST:
          serviceImpl.crashList((idb.CrashLogQuery) request,
              (io.grpc.stub.StreamObserver<idb.CrashLogResponse>) responseObserver);
          break;
        case METHODID_CRASH_SHOW:
          serviceImpl.crashShow((idb.CrashShowRequest) request,
              (io.grpc.stub.StreamObserver<idb.CrashShowResponse>) responseObserver);
          break;
        case METHODID_DESCRIBE:
          serviceImpl.describe((idb.TargetDescriptionRequest) request,
              (io.grpc.stub.StreamObserver<idb.TargetDescriptionResponse>) responseObserver);
          break;
        case METHODID_DISCONNECT:
          serviceImpl.disconnect((idb.DisconnectRequest) request,
              (io.grpc.stub.StreamObserver<idb.DisconnectResponse>) responseObserver);
          break;
        case METHODID_FOCUS:
          serviceImpl.focus((idb.FocusRequest) request,
              (io.grpc.stub.StreamObserver<idb.FocusResponse>) responseObserver);
          break;
        case METHODID_LIST_APPS:
          serviceImpl.listApps((idb.ListAppsRequest) request,
              (io.grpc.stub.StreamObserver<idb.ListAppsResponse>) responseObserver);
          break;
        case METHODID_LIST_TARGETS:
          serviceImpl.listTargets((idb.ListTargetsRequest) request,
              (io.grpc.stub.StreamObserver<idb.ListTargetsResponse>) responseObserver);
          break;
        case METHODID_LOG:
          serviceImpl.log((idb.LogRequest) request,
              (io.grpc.stub.StreamObserver<idb.LogResponse>) responseObserver);
          break;
        case METHODID_LS:
          serviceImpl.ls((idb.LsRequest) request,
              (io.grpc.stub.StreamObserver<idb.LsResponse>) responseObserver);
          break;
        case METHODID_MKDIR:
          serviceImpl.mkdir((idb.MkdirRequest) request,
              (io.grpc.stub.StreamObserver<idb.MkdirResponse>) responseObserver);
          break;
        case METHODID_MV:
          serviceImpl.mv((idb.MvRequest) request,
              (io.grpc.stub.StreamObserver<idb.MvResponse>) responseObserver);
          break;
        case METHODID_OPEN_URL:
          serviceImpl.openUrl((idb.OpenUrlRequest) request,
              (io.grpc.stub.StreamObserver<idb.OpenUrlRequest>) responseObserver);
          break;
        case METHODID_PULL:
          serviceImpl.pull((idb.PullRequest) request,
              (io.grpc.stub.StreamObserver<idb.PullResponse>) responseObserver);
          break;
        case METHODID_RM:
          serviceImpl.rm((idb.RmRequest) request,
              (io.grpc.stub.StreamObserver<idb.RmResponse>) responseObserver);
          break;
        case METHODID_SCREENSHOT:
          serviceImpl.screenshot((idb.ScreenshotRequest) request,
              (io.grpc.stub.StreamObserver<idb.ScreenshotResponse>) responseObserver);
          break;
        case METHODID_SET_LOCATION:
          serviceImpl.setLocation((idb.SetLocationRequest) request,
              (io.grpc.stub.StreamObserver<idb.SetLocationResponse>) responseObserver);
          break;
        case METHODID_TERMINATE:
          serviceImpl.terminate((idb.TerminateRequest) request,
              (io.grpc.stub.StreamObserver<idb.TerminateResponse>) responseObserver);
          break;
        case METHODID_UNINSTALL:
          serviceImpl.uninstall((idb.UninstallRequest) request,
              (io.grpc.stub.StreamObserver<idb.UninstallResponse>) responseObserver);
          break;
        case METHODID_XCTEST_LIST_BUNDLES:
          serviceImpl.xctestListBundles((idb.XctestListBundlesRequest) request,
              (io.grpc.stub.StreamObserver<idb.XctestListBundlesResponse>) responseObserver);
          break;
        case METHODID_XCTEST_LIST_TESTS:
          serviceImpl.xctestListTests((idb.XctestListTestsRequest) request,
              (io.grpc.stub.StreamObserver<idb.XctestListTestsResponse>) responseObserver);
          break;
        case METHODID_XCTEST_RUN:
          serviceImpl.xctestRun((idb.XctestRunRequest) request,
              (io.grpc.stub.StreamObserver<idb.XctestRunResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ADD_MEDIA:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.addMedia(
              (io.grpc.stub.StreamObserver<idb.AddMediaResponse>) responseObserver);
        case METHODID_DEBUGSERVER:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.debugserver(
              (io.grpc.stub.StreamObserver<idb.DebugServerResponse>) responseObserver);
        case METHODID_HID:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.hid(
              (io.grpc.stub.StreamObserver<idb.HIDResponse>) responseObserver);
        case METHODID_INSTALL:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.install(
              (io.grpc.stub.StreamObserver<idb.InstallResponse>) responseObserver);
        case METHODID_INSTRUMENTS_RUN:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.instrumentsRun(
              (io.grpc.stub.StreamObserver<idb.InstrumentsRunResponse>) responseObserver);
        case METHODID_LAUNCH:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.launch(
              (io.grpc.stub.StreamObserver<idb.LaunchResponse>) responseObserver);
        case METHODID_PUSH:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.push(
              (io.grpc.stub.StreamObserver<idb.PushResponse>) responseObserver);
        case METHODID_RECORD:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.record(
              (io.grpc.stub.StreamObserver<idb.RecordResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class CompanionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CompanionServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return idb.Idb.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CompanionService");
    }
  }

  private static final class CompanionServiceFileDescriptorSupplier
      extends CompanionServiceBaseDescriptorSupplier {
    CompanionServiceFileDescriptorSupplier() {}
  }

  private static final class CompanionServiceMethodDescriptorSupplier
      extends CompanionServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CompanionServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CompanionServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CompanionServiceFileDescriptorSupplier())
              .addMethod(getAccessibilityInfoMethod())
              .addMethod(getAddMediaMethod())
              .addMethod(getApproveMethod())
              .addMethod(getBootMethod())
              .addMethod(getClearKeychainMethod())
              .addMethod(getConnectMethod())
              .addMethod(getContactsUpdateMethod())
              .addMethod(getCrashDeleteMethod())
              .addMethod(getCrashListMethod())
              .addMethod(getCrashShowMethod())
              .addMethod(getDebugserverMethod())
              .addMethod(getDescribeMethod())
              .addMethod(getDisconnectMethod())
              .addMethod(getFocusMethod())
              .addMethod(getHidMethod())
              .addMethod(getInstallMethod())
              .addMethod(getInstrumentsRunMethod())
              .addMethod(getLaunchMethod())
              .addMethod(getListAppsMethod())
              .addMethod(getListTargetsMethod())
              .addMethod(getLogMethod())
              .addMethod(getLsMethod())
              .addMethod(getMkdirMethod())
              .addMethod(getMvMethod())
              .addMethod(getOpenUrlMethod())
              .addMethod(getPullMethod())
              .addMethod(getPushMethod())
              .addMethod(getRecordMethod())
              .addMethod(getRmMethod())
              .addMethod(getScreenshotMethod())
              .addMethod(getSetLocationMethod())
              .addMethod(getTerminateMethod())
              .addMethod(getUninstallMethod())
              .addMethod(getXctestListBundlesMethod())
              .addMethod(getXctestListTestsMethod())
              .addMethod(getXctestRunMethod())
              .build();
        }
      }
    }
    return result;
  }
}
