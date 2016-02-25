FROM java:8-jre

LABEL kodokojo-componentType="project-manager"

ADD project-manager.jar /project/project-manager.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "/project/project-manager.jar"]