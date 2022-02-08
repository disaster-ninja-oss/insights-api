FROM nexus.kontur.io:8084/redhat/ubi8
RUN dnf -y install java-17-openjdk \
  # Cleanup
  && dnf clean all \
  && rm -vrf /var/cache/dnf /var/lib/dnf /tmp/* /var/tmp/* \
  && find /var/log -type f -exec rm -v "{}" "+"
COPY target/*.jar ./
ENTRYPOINT ["java","-jar","/insights-api-0.0.1-SNAPSHOT.jar"]