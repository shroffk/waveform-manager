# Waveform Manager Service

A simple rest service which manages an index for a set of HDF 
waveform files along with a set of tags and properties.

The tags and properties which can be added by users can be used to 
facilitate quering for files and storing additional metata data not 
included in the hdf file.

### Build and Installation

**Download and install elastic**   
Download and install elasticsearch (verision 6.8.4) from [elastic.com](https://www.elastic.co/downloads/past-releases/elasticsearch-6-8-4)

**Configure**  
You can configure the file processor which are to be included or excluded from your service
by updating the service `pom.xml`.  

The service http port and the connection details for the elastic backend are configurable via the 
applications.properties located under `/src/main/resources/applications.properties`

**Build**  
Follow build instruction for the waveform-manager packages  [here](https://github.com/shroffk/waveform-manager#build)

**Run**  
Run the service using
```java -jar target/service-waveform-index-4.6.6.jar```

Note: make sure that elastic is running and the JAVA_HOME is correctly configured and added to the PATH
### Service API

#### Search for file Index
```
GET /waveformIndex/search
Accept: application/json
Content-Type: application/json

RESPONSE: HTTP 200
Content: paginated list 
```
| Search Keyword  | Usage | Description |  
|-----------------|-------|------|
| file           | file=*file_name*\* | Find files with names matching the search pattern |  
| tags           | tags=*tag_name*\* | Find files with tag names matching the search pattern |  
| start          |                  | Find files with create date or events after the start instant |  
| end            |                  | Find files with create date or events before the end instant |   
| **Property Search** |
| properties|properties=propertyName\* | Find files with properties matching in the pattern|  
| |properties=propertyName\*.attributeName\* |  Find files with properties and attributes matching in the pattern|  
| |properties=propertyName\*.attributeName\*.attributeValue\* |  Find files with properties and attributes with values matching in the pattern|


#### Create a new Index Entry

```
PUT /waveformIndex
Accept: application/json
Content-Type: application/json

{
   "file":"file:/C:/git/waveform-manager/service-waveform-index/test_file.h5",
   "tags":[
      {
         "name":"PS-Fault"
      }
   ],
   "properties":[
      {
         "name":"fault",
         "attributes":[
            {
               "name":"id",
               "value":"1234"
            },
            {
               "name":"timestamp",
               "value":"2020-01-01 14:00:30"
            }
         ]
      }
   ],
   "pvProperties":[
       {
           "pvName": "ca://SR:C30",
           "attributes":[
               {
                   "name": "trigger",
                   "value": "true"
               }
           ]
       }
   ],
    "events":[
        {
            "name": "start",
            "instant": 0
        },
        {
            "name": "end",
            "instant": 1627417760994
        }
    ]
}

RESPONSE: HTTP 201 (Created)
```

#### Retrieve an Index

```
GET /waveformIndex?fileURI={fileURI}

Response: HTTP 200
Content: paginated list 
```

#### Add a tag to Index

```
POST /waveformIndex/add/{tagName}?fileURI={fileURI}

RESPONSE: HTTP 200 (No Content)
```
#### Remove a tag from an Index

```
DELETE /waveformIndex/remove/tags/{tagName}?fileURI={fileURI}

RESPONSE: HTTP 200 (No Content)
```

#### Add a property to Index

```
POST /waveformIndex/add/properties?fileURI={fileURI}
Accept: application/json
Content-Type: application/json


{
   "name":"fault",
   "attributes":[
      {
         "name":"id",
         "value":"12345"
      },
      {
         "name":"timestamp",
         "value":"2020-01-01 14:00:30"
      }
   ]
}

RESPONSE: HTTP 200 (No Content)
```

#### Remove a property from an Index

```
DELETE /waveformIndex/remove/properties/{propertyName}?fileURI={fileURI}

RESPONSE: HTTP 200 (No Content)
```

#### Add a pv property to Index

```
POST /waveformIndex/add/pvproperties?fileURI={fileURI}
Accept: application/json
Content-Type: application/json

{
   "pvName":"SR:C01",
   "attributes":[
      {
         "name":"plotted",
         "value":"true"
      },
      {
         "name":"avg",
         "value":"1.023"
      }
   ]
}
RESPONSE: HTTP 200 (No Content)
```


#### Remove a pv property from an Index

```
DELETE /waveformIndex/remove/pvproperties/{pvpropertyName}?fileURI={fileURI}

RESPONSE: HTTP 200 (No Content)
```