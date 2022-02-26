# Spring-Sleuth-Otel-OpenSearch
Sample Application with Spring Sleuth OpenTelemetry and OpenSearch as backend.

## Spring Sleuth Zipkin with OpenTelemetry Collector
This branch contains code for Applications with `spring-cloud-sleuth-zipkin` library, and `opentelemetry collector` component as container.

![Scenario #1](images/Scenario-1-Sleuth-Zipkin.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.harivemula</groupId>
    <artifactId>Spring-Sleuth-Otel-OpenSearch</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>Customers</module>
        <module>Products</module>
    </modules>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <java.version>17</java.version>
        <spring-cloud.version>2021.0.1</spring-cloud.version>
        <spring-cloud-sleuth-otel.version>1.1.0-M4</spring-cloud-sleuth-otel.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
 

</project>

```

### docker-compose.yaml
This docker compose has **opentelemetry** collector apart from data-prepper, opensearch, dashboards.

```yaml
#docker-compose.yaml
version: "3.7"
services:
  data-prepper:
    restart: unless-stopped
    container_name: data-prepper
    image: opensearchproject/data-prepper:latest
    volumes:
      - ./data-prepper/trace_analytics_no_ssl.yml:/usr/share/data-prepper/pipelines.yaml
      - ./data-prepper/data-prepper-config.yaml:/usr/share/data-prepper/data-prepper-config.yaml
      - ./demo/root-ca.pem:/usr/share/data-prepper/root-ca.pem
    ports:
      - "21890:21890"
    networks:
      - my_network
    depends_on:
      - opensearch
  opensearch:
    container_name: node-0.example.com
    image: opensearchproject/opensearch:latest
    environment:
      - discovery.type=single-node
      - bootstrap.memory_lock=true # along with the memlock settings below, disables swapping
      - "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m" # minimum and maximum Java heap size, recommend setting both to 50% of system RAM
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536 # maximum number of open files for the OpenSearch user, set to at least 65536 on modern systems
        hard: 65536
    ports:
      - 9200:9200
      - 9600:9600 # required for Performance Analyzer
    networks:
      - my_network
  dashboards:
    image: opensearchproject/opensearch-dashboards:latest
    container_name: opensearch-dashboards
    ports:
      - 5601:5601
    expose:
      - "5601"
    environment:
      OPENSEARCH_HOSTS: '["https://node-0.example.com:9200"]'
    depends_on:
      - opensearch
    networks:
      - my_network

  otel-collector:
    restart: unless-stopped
    image: otel/opentelemetry-collector:0.24.0
    command: ["--config=/etc/otel-collector-config.yml"]
    volumes:
      - ./data-prepper/otel-collector-config.yml:/etc/otel-collector-config.yml
      - ../demo/demo-data-prepper.crt:/etc/demo-data-prepper.crt
    ports:
      - "55680:55680"
      - "9411:9411"
    depends_on:
      - data-prepper
    networks:
      - my_network

networks:
  my_network:
```