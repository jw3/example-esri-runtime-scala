example arc runtime java with scala
===

https://developers.arcgis.com/java/latest/guide/guide.htm


### notes

- Requires Java 11
- Compile time error

```
Error:(33, 22) package io contains object and package with same name: a
one of them needs to be removed from classpath
      mapView.setMap(new ArcGISMap(Basemap.createImagery))
```

setting `-Yresolve-term-conflict:object` resolves it
- https://groups.google.com/d/msg/scala-internals/rOlwp5nc96A/Jf6YQR7H4OYJ
