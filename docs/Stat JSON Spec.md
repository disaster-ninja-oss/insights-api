# Stats Spec


Stat JSON Spec

## Root

[Type Stat](#stat)﻿
* [Axis property](vscode-resource://untitleduntitled-1 "#axis-property")
* [Meta property](vscode-resource://untitleduntitled-1 "#meta-property")
* [InitAxis property](vscode-resource://untitleduntitled-1 "#initaxis-property")
* [Overlays property](vscode-resource://untitleduntitled-1 "#overlays-property")
* [Copyrights property](vscode-resource://untitleduntitled-1 "#copyrights-property")
* [Translation property](vscode-resource://untitleduntitled-1 "#translation-property")
* [Types](vscode-resource://untitleduntitled-1 "#types")

## Axis property

 [Type Axis](vscode-resource://untitleduntitled-1 "#axis")

> Need to fix - rename it to "axes"\
> Need to fix - add id property (currently label using as id)

Contain all axes what can be constructed in app with their [divisor and denominator pairs](vscode-resource://untitleduntitled-1 "#quotient"), where divisor and denominator - some available datasets (literally - fields in tiles data); For every pair described min, average and max value in `steps` property.

Also in contains `label` - that answer how to axis named, and it currently used as id.

This is necessary in order to correctly define the "color borders" for map (mapping values to colors) and sign a legend. If a [label is specified](vscode-resource://untitleduntitled-1 "#step") in addition to the values, it will be used in the legend instead of a number.

Why it must be pre-calculated? - front-end can't calculate these values in runtime because of data baked in tiles which will be uploaded on demand.

## Correlation Rate property

`rate` property shows us how interest data contains in the axis pair.

## Meta property

[Type Meta](vscode-resource://untitleduntitled-1 "#meta")

Tell that tiles can be requested from `min_zoom` to `max_zoom`. If the map zoom is outside these limits information from the nearest border will be used.\
For example if `max_zoom: 8`, and user zoom in to `10`, client request tiles for `zoom === 8` and scale it.

## InitAxis property

[Type InitAxis](vscode-resource://untitleduntitled-1 "#initaxis")

This property used for set default legend settings, another words - what user to see when app loaded. So yes - this is just two entries from [axis property](vscode-resource://untitleduntitled-1 "#axis") - for `x` axis and for `y` axis;

## Overlays property

[Type Overlay](vscode-resource://untitleduntitled-1 "#overlay")

Currently used in disaster ninja for define available for selection set of settings. Very similar to InitAxis property but with few extra properties:
* active (boolean) - describe should be active or not be default
* name - how to display that overlay in ui controls
* description - put that text near legend
* colors - legend colors. This color also will be used for colorize layer data on map

## Copyrights property

[Type Copyrights](vscode-resource://untitleduntitled-1 "#copyrights")

Copyrights that should be displayed for the dataset.

Shape:

```
some_dataset: [paragraph_1, paragraph_2, ...paragraph_n],
some_another_dataset: [paragraph_1, paragraph_2, ...paragraph_n]
```

## Translation property

[Type Translation](vscode-resource://untitleduntitled-1 "#translations")

> Need to fix - how to choice language?

Currently client just take label and replace it by value from this dict, using label as key.

## Types

### Color

```
type Color = {
  id: string;   // A1 - C3 
  color: string; // rgb(0,0,0) - rgb(255,255,255)
}
```

### Step

```
type Step = {
  label?: string;
  value: number;
}
```

### Quotient

Divisor and denominator pair

```
type Quotient = [string, string];
```

### Axis

```
type Axis = {
  label: string;
  steps: Step[];
  quotient: Quotient[];
}
```

### CorrelationRate

```
type CorrelationRate = {
  x: Axis;
  y: Axis;
  rate: number;
}
```

### InitAxis

```
type InitAxis = {
  x: Axis;
  y: Axis;
}
```

### Overlay

```
type Overlay = {
  name: string;
  description: string;
  active: boolean;
  color: Color[];
  x: Axis;
  y: Axis;
}
```

### Meta

```
type Meta = {
  min_zoom: number;
  max_zoom: number;
}

```

### Copyrights

```
type Copyrights = {
  [key: string]: string[];
}
```

### Translations

```
type Translations = {
  [key: string]: string;
}
```

### Stat

```
interface Stat {
  axis: Axis[];
  meta: Meta;
  initAxis: InitAxis;
  correlationRates: CorrelationRate[];
  overlays: Overlay[];
  copyrights: Copyrights;
  translations: Translations;
}
```
