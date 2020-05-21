# Waveform Manager Service

A simple rest service which manages an index for a set of HDF 
waveform files along with a set of tags and properties.

The tags and properties which can be added by users can be used to 
facilitate quering for files and storing additional metata data not 
included in the hdf file.

### services API

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

   ]
}

RESPONSE: HTTP 201 (Created)
Location header: http://localhost:8090/example/v1/hotels/1
```

#### Retrieve an Index

```
GET /waveformIndex/{fileURI}

Response: HTTP 200
Content: paginated list 
```

#### Add a tag to Index

```
POST /waveformIndex/{fileURI}/add/{tagName}

RESPONSE: HTTP 200 (No Content)
```
#### Remove a tag from an Index

```
DELETE /waveformIndex/{fileURI}/remove/tags/{tagName}

RESPONSE: HTTP 200 (No Content)
```

#### Add a property to Index

```
POST /waveformIndex/{fileURI}/add/properties
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
DELETE /waveformIndex/{fileURI}/remove/properties/{propertyName}

RESPONSE: HTTP 200 (No Content)
```

#### Add a pv property to Index

```
POST /waveformIndex/{fileURI}/add/pvproperties
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
DELETE /waveformIndex/{fileURI}/remove/pvproperties/{pvpropertyName}

RESPONSE: HTTP 200 (No Content)
```