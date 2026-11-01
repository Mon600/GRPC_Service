package app.interceptor;

import build.buf.protovalidate.Validator;
import build.buf.protovalidate.ValidatorFactory;
import build.buf.protovalidate.exceptions.ValidationException;
import com.google.protobuf.Message;
import io.grpc.*;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;


@Component
@GlobalServerInterceptor
public class ValidateInterceptor implements ServerInterceptor {

    private final Validator validator = ValidatorFactory.newBuilder().build();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onMessage(ReqT message) {
                try {
                    if (message instanceof Message protoMessage) {
                        validator.validate(protoMessage);
                    }
                    super.onMessage(message);
                } catch (ValidationException e) {
                    throw Status.INVALID_ARGUMENT
                        .withDescription("Validation failed. Check your data.").asRuntimeException();
                } catch (Exception e) {
                    throw Status.INTERNAL
                        .withDescription("Internal validation error").asRuntimeException();
                }
            }
        };
    }
}
