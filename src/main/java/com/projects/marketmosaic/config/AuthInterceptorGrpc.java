package com.projects.marketmosaic.config;

import com.projects.marketmosaic.constants.GrpcConstants;
import io.grpc.*;

public class AuthInterceptorGrpc implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String token = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (token == null) {
            token = headers.get(Metadata.Key.of("Cookie", Metadata.ASCII_STRING_MARSHALLER));
            if(token != null) {
                token = token.split("=")[1];
            }
        }

        Context ctx = Context.current().withValue(GrpcConstants.USER_TOKEN_CTX_KEY, token);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
