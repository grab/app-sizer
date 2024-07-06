# Limitations

App Sizer is a powerful tool, but it has some limitations that users should be aware of. This document outlines the key limitations and explains their impact on the analysis results.

## Class Download Size Calculation

Calculating the exact download size of a class from an APK is challenging. App Sizer uses an approximation method:

1. We obtain a relative [size of the class definition][class-size] (termed 'raw size').
2. We use the Dex file download size that the class belongs to.
3. We derive a relative value for the class's download size using the formula:

   ```
   class's download size = class raw size * (dex download size / all classes' raw size)
   ```

This approach provides a reasonable estimation but may not be 100% accurate. Interestingly, similar tools in the community have independently developed comparable methods.

## Files Grouped Under "Others"

### resources.arsc File
The `resources.arsc` file is a special file in Android APKs containing precompiled resources (such as binary XML for strings, arrays, and other value types) in a binary format for efficient access.

- App Sizer does not analyze this file individually.
- It's grouped under the "Others" category.
- For small Android projects, this can disproportionately impact the data, potentially creating the illusion of an inefficient analysis.

### Uncategorized Files
* Any files that cannot be categorized as Java/Kotlin code, resources, native libraries, or assets are automatically distributed to the app module and grouped under the **"Others"** category.
* Any files/classes that cannot find an owner (does not belong to a module or library) are automatically distributed to the app module

## Inline Functions and Classes

The nature of [inline functions][inline-functions] and [inline value classes][inline-class] in Kotlin presents a unique challenge:

- The size contributed by inline elements is calculated and distributed to where they are used, not where the inline methods/classes are created.
- Build systems or optimization tools like R8 might rewrite code for efficiency, including inlining methods, which can result in similar outcomes to inline functions.

This behavior can make it difficult to accurately attribute size contributions to specific modules or libraries.

## Impact on Analysis

These limitations mean that App Sizer's results should be interpreted as close approximations rather than exact measurements. They are most useful for:

- Identifying trends in app size growth
- Comparing relative size contributions of different components
- Spotting large, unexpected size increases

Users should keep these limitations in mind when making decisions based on App Sizer's output, especially for small projects or when dealing with inline-heavy codebases.

[class-size]: https://github.com/JesusFreke/smali/blob/master/dexlib2/src/main/java/org/jf/dexlib2/dexbacked/DexBackedClassDef.java#L505
[inline-functions]: https://kotlinlang.org/docs/inline-functions.html
[inline-class]: https://kotlinlang.org/docs/inline-classes.html