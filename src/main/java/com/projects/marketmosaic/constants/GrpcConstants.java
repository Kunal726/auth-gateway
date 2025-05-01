package com.projects.marketmosaic.constants;

import io.grpc.Context;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GrpcConstants {
    public static final Context.Key<String> USER_TOKEN_CTX_KEY = Context.key("user-token");
}
