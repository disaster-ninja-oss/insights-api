# Multivariate Analysis (MVA)

## Introduction

Multivariate Analysis (MVA) extends the existing Multi-Criteria Decision Analysis (MCDA) functionality by allowing users to merge and visualize spatial layers in five dimensions. These dimensions provide deeper insights by encoding multiple factors into a single, interpretable visualization.


## **Key Dimensions**

MVA introduces five primary dimensions:
* **Annex Axis (Score):** Represents the standard MCDA good → bad scale.
* **Base Axis (Compare):** Represents an additional sentiment layer, mapped on an unimportant → important scale.
* **Opacity (Hide Areas):** Determines hex transparency based on data values.
* **Hex Height (3D):** Adds an extrusion effect to hexagons based on values.
* **Labels:** Displays specific numeric values inside hexagons.

Each dimension is represented by MCDA config (outlier removal → transformation → outlier removal → normalization).

## **Solution Considerations**
* Users can add MCDA layers to analysis and select appearance options (dimensions) for each layer.
* We decided not to pull all variety of current layers sentiments into MVA - dimensions sentiments are independent of children layers sentiments. Only one sentiment is used for the Score and Compare dimensions
* Bivariate matrix 

  In the layers panel:
  * The legend and analysis name are shown, but individual layers are not listed.
  * Clicking on an analysis opens the configuration popup.


# **Behavior of Dimensions**

## **Score (Annex Axis)**

* Generates a red-yellow-green color gradient using MCDA mechanics.

### **Hide Areas (Opacity)**

* Transparency is applied based on a three-step legend:
  * **0–0.33 → 10% opacity**
  * **0.34–0.66 → 40% opacity**
  * **0.67–1 → 70% opacity**

### **Compare (Base Axis)**

* When Compare is included, a **3×3 bivariate color scheme** is applied.
* The Score dimension becomes the annex axis (good-bad sentiment).
* The Compare dimension becomes the base axis (unimportant-important sentiment).
* Stops for axis scoring:
  * **0 → 0.33 → 0.67 → 1**

### **3D (Hex Height)**

* Height is calculated based on the **final score** of participating layers.
* **Max height is determined during development.**

### **Labels**

* Displays values for selected criteria.
* If multiple layers contribute, values are shown separately with units.


## **Corner Cases**

* **No Score Dimension:** Coloring follows the Score dimension logic.
* **No layers affecting color:** Default hex color is **#999999**.
* **If there is only one layer in Score, Compare, or Hide dimension**, the legend should be split into 10 steps, displaying real values at each step.
