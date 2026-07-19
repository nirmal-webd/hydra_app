# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in in the Android SDK default Proguard rules file.

# Preserve Kotlin data classes for Room/Datastore reflection if necessary,
# but R8 usually handles this well with standard libraries.

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
