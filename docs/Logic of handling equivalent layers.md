# Logic of handling equivalent layers

Field: Content

## Handling equivalent layers

Bivariate matrix contains many layers (different numbers on annex and base sides as the annex side doesn't contain layers with base nominators).

#### **The initial aim** 

To make a bivariate matrix smaller, and easy to read, in case we have 100+ layers. 

Solution: It was decided to collapse some (equivalent) layers. The idea is that layers with high correlation (>=0.8, can be adjusted) contain similar data and for analysis purposes, we can show them as one layer (not fully hide, collapse only).

Technically, some layers can have a parent layer and be hidden under them. As we have a different set of layers on the base and annex sides, one layer can have different parents on different sides of the matrix.

Also, we guess, that some layers for some users can be important and we shouldn't collapse such layers. (currently, the list with such layers is hardcoded but can be adjusted by the user in the future).
* 

#### **The process of finding the parent layer (BE side):**

We have a table with layers (layer = 2 indicators =1 numerator + 1 denominator) and their correlation for the whole world (table `bivariate_axis_correlation`):
* x_num
* y_num
* x_den
* y_den
* correlation

After polygon selection, we also have a list of correlations between layers for the exact area. 

Also, there is an avg_correlation for each layer for annex and base sides (calculated "on the fly").

High-level algorithm for grouping layers:
* find all pairs of layers with correlations >= 0.8 (can be configured)
* combine them into groups:
  * we should have different groups for annex and base matrix sides (it is because some layers have a basic numerator and they exist on a basic side only)
  * layers with no correlation >=0.8 will not fall into any groups
  * the layer should be in a group where at least one layer has a correlation >=0.8 with this layer
* find in each group a layer with a lower avg_correlation → set it as a parent of all layers in the group
  * need to take avg_correlation from the same axis

We have additional logic to not hide important layers (the user can request some layers) so that the important layer should not have a parent. It should become the parent of the group if there are no more important layers in the group, or create a separate group with only one layer inside.

#### **Example**
* List of input layers:**

\["avgmax_ts" ,"one"\]

\["covid19_confirmed" ,"area_km2"\]

\["gdp" ,"area_km2"\]

\["mandays_maxtemp_over_32c_1c" ,"area_km2"\]

\["population_prev" ,"area_km2"\]

\["population" ,"area_km2"\] *(layer has a base numerator)*
* Interesting Layers:** \[\["population", "area_km2"\], \["population_prev" ,"area_km2"\]\]

List of correlations:

|     |     |     |     |     |
| --- | --- | --- | --- | --- |
| x_num | x_den | y_num | y_den | correlation |
| "avgmax_ts" | "one" | "population" | "area_km2" | 0.5659504016 |
| "gdp" | "area_km2" | "avgmax_ts" | "one" | 0.5680131412 |
| "avgmax_ts" | "one" | "gdp" | "area_km2" | 0.5680131412 |
| "population_prev" | "area_km2" | "avgmax_ts" | "one" | 0.5659351894 |
| "avgmax_ts" | "one" | "population_prev" | "area_km2" | 0.5659351894 |
| "mandays_maxtemp_over_32c_1c" | "area_km2" | "avgmax_ts" | "one" | 0.5583243285 |
| "avgmax_ts" | "one" | "mandays_maxtemp_over_32c_1c" | "area_km2" | 0.5583243285 |
| "covid19_confirmed" | "area_km2" | "avgmax_ts" | "one" | 0.5659468068 |
| "avgmax_ts" | "one" | "covid19_confirmed" | "area_km2" | 0.5659468068 |
| "gdp" | "area_km2" | "population" | "area_km2" | 0.999995785 |
| "gdp" | "area_km2" | "covid19_confirmed" | "area_km2" | 0.9999955442 |
| "covid19_confirmed" | "area_km2" | "gdp" | "area_km2" | 0.9999955442 |
| "gdp" | "area_km2" | "mandays_maxtemp_over_32c_1c" | "area_km2" | 0.9958112299 |
| "mandays_maxtemp_over_32c_1c" | "area_km2" | "gdp" | "area_km2" | 0.9958112299 |
| "gdp" | "area_km2" | "population_prev" | "area_km2" | 0.9999957192 |
| "population_prev" | "area_km2" | "gdp" | "area_km2" | 0.9999957192 |
| "covid19_confirmed" | "area_km2" | "population" | "area_km2" | 0.999999665 |
| "covid19_confirmed" | "area_km2" | "mandays_maxtemp_over_32c_1c" | "area_km2" | 0.9958323165 |
| "mandays_maxtemp_over_32c_1c" | "area_km2" | "covid19_confirmed" | "area_km2" | 0.9958323165 |
| "covid19_confirmed" | "area_km2" | "population_prev" | "area_km2" | 0.9999996616 |
| "population_prev" | "area_km2" | "covid19_confirmed" | "area_km2" | 0.9999996616 |
| "population_prev" | "area_km2" | "population" | "area_km2" | 0.9999999671 |
| "population_prev" | "area_km2" | "mandays_maxtemp_over_32c_1c" | "area_km2" | 0.9958347647 |
| "mandays_maxtemp_over_32c_1c" | "area_km2" | "population_prev" | "area_km2" | 0.9958347647 |
| "mandays_maxtemp_over_32c_1c" | "area_km2" | "population" | "area_km2" | 0.9958354739 |
* Base axis groups:**

Parent layer for group 1: \["population" ,"area_km2"\]

|     |
| --- |
| Basic axis |
| Group 1 | avg_corr (base axis) | is_interesting |
| "covid19_confirmed" | "area_km2" | 0.4041426712 |  |
| "gdp" | "area_km2" | 0.4009065158 |  |
| "mandays_maxtemp_over_32c_1c" | "area_km2" | 0.4007926055 |  |
| "population" | "area_km2" | 0.408212714 | yes |
| Group 2 | avg_corr (base axis) | is_interesting |
| "population_prev" | "area_km2" | 0.4020669498 | yes |
| Group 3 | avg_corr (base axis) | is_interesting |
| "avgmax_ts" | "one" | 0.4182900054 |  |
* Annex axis groups**

Parent layer for group 1:  \["population_prev" ,"area_km2"\]

|     |
| --- |
| Annex axis |
| Group 1 | avg_corr (annex axis) | is_interesting |
| "covid19_confirmed" | "area_km2" | 0.4192068651 |  |
| "gdp" | "area_km2" | 0.4133671242 |  |
| "mandays_maxtemp_over_32c_1c" | "area_km2" | 0.4157415767 |  |
| "population_prev" | "area_km2" | 0.4173316022 | yes |
| Group 2 | avg_corr (annex axis) | is_interesting |
| "avgmax_ts" | "one" | 0.3995569464 |  |

#### **Works with NO DATA**

When we do not have any data for some specific geometry in the layer, we do not show this layer in the matrix. 

Technically it means that for geometry in this layer max_value=min_value.
* Note:** if there are "important" layers for which there is no data → we do not show them in the matrix. This means that the rule "hide layers without data" is more important than "show layers that are important to the user." We will check if this rule will be user-friendly in the future.

#### **Open questions:**
* do we need to calculate a correlation between layers with the same indicators and different denominators (forest/population VS forest/area) 

#### **Up next:**
* need user profile to store and manage which indicators are useful for this user

The takeaway from math: if the correlation is high, we can show a layer as y=kx+b of another layer - we don't need the whole map, just K and B.  <https://www.postgresql.org/docs/current/functions-aggregate.html>  \
regr_intercept and regr_slope from Postgres will give these values

|     |
| --- |
| `regr_intercept` ( *`Y`* `double precision`, *`X`* `double precision` ) → `double precision` Computes the y-intercept of the least-squares-fit linear equation determined by the (*`X`*, *`Y`*) pairs. |
| `regr_slope` ( *`Y`* `double precision`, *`X`* `double precision` ) → `double precision` Computes the slope of the least-squares-fit linear equation determined by the (*`X`*, *`Y`*) pairs. |

