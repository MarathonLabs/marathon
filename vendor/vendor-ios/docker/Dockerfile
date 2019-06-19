FROM ibmcom/swift-ubuntu:4.2.4

COPY collect-dependencies.sh find-executable.sh /tmp/
RUN cp -avL `/bin/bash /tmp/find-executable.sh llvm-nm | head -1` /bin/llvm-nm \
      && /bin/bash /tmp/collect-dependencies.sh /bin/llvm-nm /tmp/llvm-nm.tar.gz \
      && /bin/bash /tmp/collect-dependencies.sh /usr/bin/swift-demangle /tmp/swift-demangle.tar.gz

FROM alpine:3.9

COPY --from=0 /tmp/llvm-nm.tar.gz /tmp/swift-demangle.tar.gz /tmp/
COPY list-test-methods.sh /usr/bin/list-test-methods

RUN chmod 0755 /usr/bin/list-test-methods \
  && apk update && apk add tar \
  && rm -Rf /var/cache/apk/* \
  && tar x --skip-old-files -zvf /tmp/llvm-nm.tar.gz \
  && tar x --skip-old-files -zvf /tmp/swift-demangle.tar.gz \
  && apk del -r tar \
  && rm -Rf /tmp/*

ENTRYPOINT ["/usr/bin/list-test-methods"]
