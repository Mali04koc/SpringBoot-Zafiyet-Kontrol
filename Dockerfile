# ------------------------------------------------------------------
# VULN-IAC-1: Eski/EOL base image.
# openjdk:8-jdk-alpine artik bakim almiyor (EOL) ve bilinen OS paket
# zafiyetleri barindirir.
# FIX: eclipse-temurin:21-jre-alpine gibi guncel, bakimi suren bir
# base image kullan.
# ------------------------------------------------------------------
FROM openjdk:8-jdk-alpine

# ------------------------------------------------------------------
# VULN-IAC-2: Build-time secret, image layer'ina gomuluyor.
# ARG/ENV ile verilen secret, "docker history" ile sonradan
# herkes tarafindan okunabilir.
# FIX: secret'i build sirasinda --secret (BuildKit) ile gec,
# image icine asla ENV/ARG olarak gomme.
# ------------------------------------------------------------------
ARG DB_PASSWORD=hardcoded_build_secret_123
ENV DB_PASSWORD=${DB_PASSWORD}

WORKDIR /app

# VULN-IAC-3: ADD yerine COPY kullanilmali (ADD gereksiz ozellikler
# - uzak URL indirme, otomatik arsiv acma - icerir ve saldiri yuzeyini buyutur).
ADD target/vuln-spring-demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# ------------------------------------------------------------------
# VULN-IAC-4: USER tanimlanmamis, container varsayilan olarak root
# kullaniciyla calisiyor. Container kacisi (escape) durumunda host
# uzerinde root yetkisi riski olusturur.
# FIX: "RUN addgroup -S app && adduser -S app -G app" ile non-root
# bir kullanici olustur ve "USER app" ile calistir.
# ------------------------------------------------------------------
ENTRYPOINT ["java", "-jar", "app.jar"]
