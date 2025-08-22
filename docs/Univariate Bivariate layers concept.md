# Univariate/Bivariate layers concept

Field: Content

### Previous approach:
* We don't have an opportunity to view univariate layers on the map (examples of univariate layers: Population density, Air temperature, etc.). Only if we select 2 identical layers in the bivariate matrix we can see a kinda univariate layer (but it takes time, it's not obvious, and not all users have access to the matrix).
* We show bivariate layers as presets (=bivariate overlays) on [[Tasks/project: Disaster Ninja#^131cd711-3bb4-11e9-9428-04d77e8d50cb/e23db600-3e7d-11e9-83ef-692ac8203621]] as well as in the bivariate matrix (please, see this document with terms related to bivariate layers, matrix, etc. [[Tasks/project: Bivariate Manager#^131cd711-3bb4-11e9-9428-04d77e8d50cb/c4a79070-96cd-11eb-83ce-250e9b944601]]).
  * bivariate presets are hardcoded in Geocint pipeline and then are transferred to insights DB
  * DN-BE provides all layers to FE via endpoints **POST[/layers/search/global](https://test-apps-ninja02.konturlabs.com/active/api/swagger-ui/index.html?configUrl=/active/api/v3/api-docs/swagger-config#/Layers/getGlobalLayers)** and **POST[/layers/search/selected_area](https://test-apps-ninja02.konturlabs.com/active/api/swagger-ui/index.html?configUrl=/active/api/v3/api-docs/swagger-config#/Layers/getSelectedAreaLayers)** (for global and local layers) but gathers information about layers from different places (Layers API, Insights API, other)
  * data structure of bivariate presets (stored in insights DB)

|     |     |
| --- | --- |
| **Column** | **Description** |
| ord | Sequence number of the layer in the list of bivariate presets |
| name | Name of the layer which is displayed on FE |
| description | Description of the layer which is displayed on FE |
| x_numerator | Two indicators to construct layer (univariate) for the annex axis (vertical) of bivariate legend: x_numerator/x_denominator |
| x_denominator |
| y_denominator | Two indicators to construct layer (univariate) for the base axis (horizontal) of bivariate legend: y_numerator/y_denominator |
| y_numerator |
| active | Flag to identify which layer is turned on by default |
| colors | Legend of the layers: colors for all 9 cells of bivariate legend.  Example:  \[{"id": "A1", "color": "rgb(232,232,157)"}, {"id": "A2", "color": "rgb(239,163,127)"}, {"id": "A3", "color": "rgb(228,26,28)"}, {"id": "B1", "color": "rgb(186,226,153)"}, {"id": "B2", "color": "rgb(161,173,88)"}, {"id": "B3", "color": "rgb(191,108,63)"}, {"id": "C1", "color": "rgb(90,200,127)"}, {"id": "C2", "color": "rgb(112,186,128)"}, {"id": "C3", "color": "rgb(83,152,106)"}\]  |
| application | Application (e.g. Disaster.Ninja, Smart City, etc) that displays this layer **\[not used\]** |
| is_public | Flag to identify if the layer can be displayed for not authorized users. |

It was decided to have **one source of truth** with available **layers**. As a result, **Layers DB** is a place to store all layers, and Layers API is an interface to get all layers. 

### Current approach:

#### Storage of layer attributes

*The approach is pretty much the same for univariate and bivariate layers, differences will be highlighted below.*
* Layers DB (schema is described in this document [[---#^b2d59af0-3b70-11e9-be77-04d77e8d50cb/1ec9d490-6159-11ec-aa24-d7e4bcd7bd6f]]) is a source of truth for layers. It stores a list of bivariate presets and univariate presets. Anyway, insights-api still provides vector tiles with hexagons geometry and data inside. URL of the endpoint to get tiles will be stored in Layers DB (url field). 
* List of attributes that will be **stored** in Layers DB for univariate and bivariate layers:
  * list of indicators for the layer:
    * `x_numerator`
    * `x_denominator`
    * `y_denominator` - *for bivariate presets only*
    * `y_numerator` - *for bivariate presets only*
  * layer `name`
  * layer `description`
  * layer `order` (separate for univariate and bivariate layers)
  * `min/max zoom` (currently hardcoded \[0;8\] but we will have variable zooms after a while)
  * `legend`/colors <not stored in insights-db directly but can be calculated using directions and colors from insights>
* List of attributes that are **not** stored in Layers DB but requested from Insights api:
  * `last_updated`
  * `copyrights`
  * source_updated?
  * additional/specific fields
    * `labels` (for legend) - exact numbers for legend steps
    * `quotients` (with nested fields as `labels` , `direction`) - to show tooltips for the bivariate legend and add axes' labels - *for bivariate presets only*

#### Sequence diagram with requests flow for layers

![image.png](https://kontur.fibery.io/api/files/165d08cd-ef50-469c-9f9b-b831bd00def4#width=1880&height=924 "")

#### Tiles generation

As previously mentioned, tiles generation is still insights-api responsibility. But the only change is that we specify in the tiles request list of indicators that are included in the selected bivariate or univariate layer and should be returned. 

It means that the number of indicators can be from 2 to 4 (for now) in this request.

#### Process of adding/editing layers list

To unload or change the list of univariate/bivariate layers in Layers DB we should run Layers DB ETL process with changes.

Example of file that should be provided from insights to layers to generate a new preset (univariate example): <https://docs.google.com/spreadsheets/d/17oNKHr44P9cW4ectLhceOgKN_fAbgsZipMidzwFUl84/edit?usp=sharing>
