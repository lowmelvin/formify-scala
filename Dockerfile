FROM sbtscala/scala-sbt:graalvm-ce-22.3.0-b2-java17_1.9.2_3.3.0 as sbt-graalvm
WORKDIR /app
COPY . .

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
RUN sbt assembly

RUN gu install native-image
RUN native-image  \
    --no-fallback  \
    --enable-http  \
    --enable-https  \
    --static \
    -jar target/**/formify.jar

FROM scratch
COPY --from=sbt-graalvm /app/formify /app/formify
ENTRYPOINT ["/app/formify"]