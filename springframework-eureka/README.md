# Build Docker Images

```
mvn clean package docker:build
```

# Start Discovery Server

```
docker run -it --rm --name eureka-server -p 8761:8761 freestrings/eureka-server
```

# Start Eureka Client

```
docker run -it --rm --name eureka-client1 --link eureka-server -p 9990:8080 freestrings/eureka-client
docker run -it --rm --name eureka-client2 --link eureka-server -p 9991:8080 freestrings/eureka-client
```

# Check Apps

```
curl http://localhost:8761/eureka/apps
<applications>
  <versions__delta>1</versions__delta>
  <apps__hashcode>UP_2_</apps__hashcode>
  <application>
    <name>EUREKA-CLIENT</name>
    <instance>
      <instanceId>98965598a897:eureka-client</instanceId>
      <hostName>172.17.0.3</hostName>
      <app>EUREKA-CLIENT</app>
      <ipAddr>172.17.0.3</ipAddr>
      <status>UP</status>
      <overriddenstatus>UNKNOWN</overriddenstatus>
      <port enabled="true">8080</port>
      <securePort enabled="false">443</securePort>
      <countryId>1</countryId>
      <dataCenterInfo class="com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo">
        <name>MyOwn</name>
      </dataCenterInfo>
      <leaseInfo>
        <renewalIntervalInSecs>30</renewalIntervalInSecs>
        <durationInSecs>90</durationInSecs>
        <registrationTimestamp>1468908985449</registrationTimestamp>
        <lastRenewalTimestamp>1468909344269</lastRenewalTimestamp>
        <evictionTimestamp>0</evictionTimestamp>
        <serviceUpTimestamp>1468908984747</serviceUpTimestamp>
      </leaseInfo>
      <metadata class="java.util.Collections$EmptyMap"/>
      <homePageUrl>http://172.17.0.3:8080/</homePageUrl>
      <statusPageUrl>http://172.17.0.3:8080/info</statusPageUrl>
      <healthCheckUrl>http://172.17.0.3:8080/health</healthCheckUrl>
      <vipAddress>eureka-client</vipAddress>
      <secureVipAddress>eureka-client</secureVipAddress>
      <isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>
      <lastUpdatedTimestamp>1468908985449</lastUpdatedTimestamp>
      <lastDirtyTimestamp>1468908983668</lastDirtyTimestamp>
      <actionType>ADDED</actionType>
    </instance>
    <instance>
      <instanceId>502856de61ae:eureka-client</instanceId>
      <hostName>172.17.0.4</hostName>
      <app>EUREKA-CLIENT</app>
      <ipAddr>172.17.0.4</ipAddr>
      <status>UP</status>
      <overriddenstatus>UNKNOWN</overriddenstatus>
      <port enabled="true">8080</port>
      <securePort enabled="false">443</securePort>
      <countryId>1</countryId>
      <dataCenterInfo class="com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo">
        <name>MyOwn</name>
      </dataCenterInfo>
      <leaseInfo>
        <renewalIntervalInSecs>30</renewalIntervalInSecs>
        <durationInSecs>90</durationInSecs>
        <registrationTimestamp>1468909056242</registrationTimestamp>
        <lastRenewalTimestamp>1468909326222</lastRenewalTimestamp>
        <evictionTimestamp>0</evictionTimestamp>
        <serviceUpTimestamp>1468909055730</serviceUpTimestamp>
      </leaseInfo>
      <metadata class="java.util.Collections$EmptyMap"/>
      <homePageUrl>http://172.17.0.4:8080/</homePageUrl>
      <statusPageUrl>http://172.17.0.4:8080/info</statusPageUrl>
      <healthCheckUrl>http://172.17.0.4:8080/health</healthCheckUrl>
      <vipAddress>eureka-client</vipAddress>
      <secureVipAddress>eureka-client</secureVipAddress>
      <isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>
      <lastUpdatedTimestamp>1468909056242</lastUpdatedTimestamp>
      <lastDirtyTimestamp>1468909055657</lastDirtyTimestamp>
      <actionType>ADDED</actionType>
    </instance>
  </application>
</applications>
```

# Check Instance

```
curl -s http://localhost:9999/service-instances/eureka-client | python -m json.tool
[
    {
        "host": "172.17.0.4",
        "instanceInfo": {
            "actionType": "ADDED",
            "app": "EUREKA-CLIENT",
            "appGroupName": null,
            "asgName": null,
            "countryId": 1,
            "dataCenterInfo": {
                "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                "name": "MyOwn"
            },
            "healthCheckUrl": "http://172.17.0.4:8080/health",
            "homePageUrl": "http://172.17.0.4:8080/",
            "hostName": "172.17.0.4",
            "instanceId": "a504ff69d731:eureka-client",
            "ipAddr": "172.17.0.4",
            "isCoordinatingDiscoveryServer": false,
            "lastDirtyTimestamp": 1468909437739,
            "lastUpdatedTimestamp": 1468909438391,
            "leaseInfo": {
                "durationInSecs": 90,
                "evictionTimestamp": 0,
                "lastRenewalTimestamp": 1468909438391,
                "registrationTimestamp": 1468909438391,
                "renewalIntervalInSecs": 30,
                "serviceUpTimestamp": 1468909437874
            },
            "metadata": {},
            "overriddenStatus": "UNKNOWN",
            "secureHealthCheckUrl": null,
            "secureVipAddress": "eureka-client",
            "sid": "na",
            "status": "UP",
            "statusPageUrl": "http://172.17.0.4:8080/info",
            "vipAddress": "eureka-client"
        },
        "metadata": {},
        "port": 8080,
        "secure": false,
        "serviceId": "EUREKA-CLIENT",
        "uri": "http://172.17.0.4:8080"
    },
    {
        "host": "172.17.0.3",
        "instanceInfo": {
            "actionType": "ADDED",
            "app": "EUREKA-CLIENT",
            "appGroupName": null,
            "asgName": null,
            "countryId": 1,
            "dataCenterInfo": {
                "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                "name": "MyOwn"
            },
            "healthCheckUrl": "http://172.17.0.3:8080/health",
            "homePageUrl": "http://172.17.0.3:8080/",
            "hostName": "172.17.0.3",
            "instanceId": "98965598a897:eureka-client",
            "ipAddr": "172.17.0.3",
            "isCoordinatingDiscoveryServer": false,
            "lastDirtyTimestamp": 1468908983668,
            "lastUpdatedTimestamp": 1468908985449,
            "leaseInfo": {
                "durationInSecs": 90,
                "evictionTimestamp": 0,
                "lastRenewalTimestamp": 1468909494300,
                "registrationTimestamp": 1468908985449,
                "renewalIntervalInSecs": 30,
                "serviceUpTimestamp": 1468908984747
            },
            "metadata": {},
            "overriddenStatus": "UNKNOWN",
            "secureHealthCheckUrl": null,
            "secureVipAddress": "eureka-client",
            "sid": "na",
            "status": "UP",
            "statusPageUrl": "http://172.17.0.3:8080/info",
            "vipAddress": "eureka-client"
        },
        "metadata": {},
        "port": 8080,
        "secure": false,
        "serviceId": "EUREKA-CLIENT",
        "uri": "http://172.17.0.3:8080"
    }
]
```