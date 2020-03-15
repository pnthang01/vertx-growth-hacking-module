FROM openjdk:10

RUN mkdir /production
WORKDIR /production
COPY ./build/docker /production

EXPOSE 80
EXPOSE 11910

CMD ["java", "-jar", "-server", "-Dis_cluster=true", "-Dstarter_class=vn.panota.growth.GrowthHackStarter","-Dvertx.zookeeper.config=./config/zookeeper-conf.json", "./growth-hacking-module-1.0.jar"]
