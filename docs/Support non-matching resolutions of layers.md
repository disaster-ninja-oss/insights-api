# Support non-matching resolutions of layers

### Context:

Layers with different zoom levels / spatial resolution domains could be presented together on Kontur's platform and we want to be able to analyze them together.

Actual examples:
* The typical layer we operate with at Kontur's platform was originally presented at 8th resolution. We generate and store overviews for all previous zoom levels, so we have 8..0 zooms for most of the layers (so far).
* PDC layers are originally presented at 4 resolution (so we have 4…0 zooms)
* REOR20 layers are much more detailed (up to 14th resolution)

We should analyze possible problems connected with joint analysis of layers with different zoom levels base. How to calculate statistics? How to show tiles on map?

### General conditions
* We **always** generate all previous zoom levels, and we require it from user-uploaded datasets also. It means that we will never have layer domains like \[4..2\] or \[14..10\], they will be \[4..0\] and \[14..0\]. It also means that any layers will always have some common zoom levels.
* When generating overviews, we always care of **quality metrics** which means that correlations should be comparable when calculated at any scale. We also measure the quality of user-uploaded layers.

### Analysis

When facing two layers with different zoom levels domains we can be sure that they have common resolutions, and the common resolutions range will be equal to range of the "worst" layer e.g.:
* layer A is defined at \[8..0\] and layer B is defined at \[4..0\] - they have \[4..0\] range of common zoom levels, equal to layer's B.
* layer C is defined at \[8..0\] and layer D is defined at \[14..0\] - they have \[8..0\] common range, equal to layer's C.

it is based on the first general condition. The main conclusion from it: **we can always calculate joined statistics on the zoom level corresponding to the best zoom of the "worst" layer.** Taking into account the second general condition, it seems like there is no sense in [downscaling](https://en.wikipedia.org/wiki/Downscaling "https://en.wikipedia.org/wiki/Downscaling") "worst" layer values to better zoom levels, as long as both downscaled and upscaled (overview) values will be synthetic (strictly depended on original values) and will not significantly affect correlation.
* Resume 1**: When calculating correlations, it's enough to take existing values in intersecting part of zooms domain without any downscaling procedures. Maybe we should keep zoom levels information as metadata for each layer, to save query time.

When showing bivariate/mcda/… tiles, built from such layers, on a map, we want to allow users to look at maximum available zoom level, even if it will be synthetic. It means we should downscale values of the "worst" layer to current map zoom (if this zoom is not available in layer, but available in second one), using some mechanism. There are possible downscaling mechanisms:

1. **Just copy values of parent hex to all child hexes**. Simple variant for many variables, like air temperature etc.

![изображение.png](https://kontur.fibery.io/api/files/bf29ad71-9a13-4f7e-984b-204fca3948ae#align=%3Aalignment%2Fblock-left&width=174&height=191 "")

v

![изображение.png](https://kontur.fibery.io/api/files/b85487d2-499d-4927-9197-2d3d3d0ac093#align=%3Aalignment%2Fblock-left&width=176&height=192 "")

2. **Divide value of parent hex by child hexes proportional to area** (e.g. parent hex (zoom 7) has value 50, his 8th zoom children will have similar values \~50/7 = 7.14). Could be used for "countable" variables, like highway length

![изображение.png](https://kontur.fibery.io/api/files/bf29ad71-9a13-4f7e-984b-204fca3948ae#align=%3Aalignment%2Fblock-left&width=182&height=201 "")

v

![изображение.png](https://kontur.fibery.io/api/files/9c9fa90b-fb09-4f86-9470-7a1f3114fb81#align=%3Aalignment%2Fblock-left&width=186&height=203 "")

3. **Use interpolation taking into account parent neighbor hexes**. Interpolation could be linear, spline, etc. Useful for advanced processing of continuous environmental variables like air temperature

![изображение.png](https://kontur.fibery.io/api/files/291a29d5-0483-4600-ba11-7dcd1f08785b#align=%3Aalignment%2Fblock-left&width=218&height=217 "")

v

![изображение.png](https://kontur.fibery.io/api/files/08dc243c-135a-4737-9e6b-67a5315e6760#align=%3Aalignment%2Fblock-left&width=229&height=245 "")

4. **Use reference dataset as weighting factor**, e.g. population - where more population, there bigger part of parent hex values will be distributed. For different phenomenons different references are valid. 

*For the initial implementation, only mechanisms 1 and 2 are required*. Selection of mechanism for concrete variables should be strictly connected with average function used for creating overviews! For example, if overviews are generating like sum() of child hexes, downscaling should be performed with area-proportional dividing. It seems like it should be a layer property, specified in metadata while layer creation.
* Resume 2**: Downscaling is performing on-the-fly for tile generation only. We don't need to keep downscaled values in database, cache them or smth like that - procedures are fast and cover just map extent / tens of parent hexes. We should keep mechanism of downscaling as metadata for each layer, and it should be synchronized with aggregation function used for overview creation.
* What should be done to support non-matching resolutions:**

1. Add aggregation/downscaling mechanism metadata to layers (store functions as avg(), max(), min(), custom(), etc.) \[geocint\]
   1. function is a property of the indicator (probably, function for downscaling can be stored, for upscaling can be defined from the previous one)
   2. <https://docs.google.com/spreadsheets/d/1DROa6GSzVy7RJWVVXUU47KL7yFW-TvlkLutSKdqnLs0/edit?usp=sharing> - doc with aggregations to review (created several months ago, not all indicators are presented - need to recheck)
2. Add metadata about presented zoom levels to layers \[geocint\]
   1. it can help to calculate correlations and analytics based not on max data resolution, but on indicators metadata, which should be faster
3. Tune multi-layer calculation queries, forcing them to use only intersecting parts of zoom-level domains \[insights-api\]
   1. queries for correlation matrix, analytics panel, advanced analytics
4. Tune tile-generating queries, forcing them to perform downscaling if **only one** of the layers is not presented at requested zoom level \[insights-api\]
* Notes:**

1. Changes shouldn't affect bivariate legend as all upscaling mechanisms will work proportionally and shouldn't go beyond the limits of the maximum and minimum values.
2. (future) We should consider different resolutions for MCDA calculation. This analysis has many steps and layers with diff resolutions that will alternate:
   * resolution of MCDA analysis=max common zoom - as PoC (as the 1st iteration)
