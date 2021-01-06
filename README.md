# Insights Api
GraphQL API for calculating correlation between parameters in given polygon in wkt format and lists of numerators and denominators for x and y axes
##Test UI
Test UI is available on http://url/graphiql
##Requests
Get all Earth correlations:
```
{allStatistic(defaultParam: 1){fields}}
```
Get correlations in polygon in wkt format and lists of numerators and denominators for x and y axes:
```
{polygonStatistic(polygonStatisticRequest:{
    polygon: "POLYGON((27.048339843749996 54.06905933872848, 27.0867919921875 53.87196345747181, 27.3724365234375 53.67068019347264, 27.9217529296875 53.81362579235235, 27.872314453125 54.081951104880396, 27.4163818359375 54.19779692488548, 27.048339843749996 54.06905933872848))",
    xDenominatorList: ["count", "area_km2"],
    yDenominatorList: ["population", "area_km2"],
    xNumeratorList: ["count", "area_km2"],
    yNumeratorList: ["population", "area_km2"]
  }){fields}
```
Fields description https://gist.github.com/Akiyamka/8ad19a8de3c955ac1f27f67281c12fdf#axis