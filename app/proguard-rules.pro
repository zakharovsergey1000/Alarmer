# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class SyncTask { *; }
-keep class JSONHttpClient { *; }

-dontwarn org.xmlpull.v1.**

#-keep class android.support.v7.** { *; }   
#    -dontwarn android.support.v4.**
    -dontwarn javax.activation.**
    -dontwarn javax.security.**
    -dontwarn java.awt.**
#    -libraryjars <java.home>/lib/rt.jar
#    -keep class javax.** {*;}
#    -keep class com.sun.** {*;}
#    -keep class myjava.** {*;}
#    -keep class org.apache.harmony.** {*;}
#    -keep public class Mail {*;}        
#    -dontshrink
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.vending.licensing.**

#	-libraryjars C:\Users\Sergey\workspace\AdvancedCalendar\libs\joda-time-2.3.jar
-dontwarn org.joda.time.**
-dontwarn com.thoughtworks.xstream.**
-keep class FileUploadResponse { *; }
#-keep class AuthenticateResponse { *; }
#-keep class UserProfile { *; }

#greenrobot.dao specifics
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

#ACRA specifics
-dontwarn org.acra.**
# Restore some Source file names and restore approximate line numbers in the stack traces,
# otherwise the stack traces are pretty useless
-keepattributes SourceFile,LineNumberTable

# keep this class so that logging will show 'ACRA' and not a obfuscated name like 'a'.
# Note: if you are removing log messages elsewhere in this file then this isn't necessary
-keep class org.acra.ACRA {
    *;
}

# keep this around for some enums that ACRA needs
-keep class org.acra.ReportingInteractionMode {
    *;
}

-keepnames class org.acra.sender.HttpSender$** {
    *;
}

-keepnames class org.acra.ReportField {
    *;
}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter {
    public void addCustomData(java.lang.String,java.lang.String);
    public void putCustomData(java.lang.String,java.lang.String);
    public void removeCustomData(java.lang.String);
}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter {
    public void handleSilentException(java.lang.Throwable);
}

-keep class org.xmlpull.v1.** { *; }
#-keep class org.** { *; }
#-keep class biz.advancedcalendar.wsdl.** { *; }
-keep class android.support.** { *; }

##---------------Begin: proguard configuration common for all Android apps ----------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-allowaccessmodification
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-repackageclasses ''

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
  public static <fields>;
}

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class * {
    public protected *;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
##---------------End: proguard configuration common for all Android apps ----------

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

-keepattributes EnclosingMethod

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keepattributes InnerClasses

# Add any classes the interact with gson

-keep class biz.advancedcalendar.activities.accessories.InformationUnitMatrix { *; }
-keep class biz.advancedcalendar.activities.accessories.InformationUnitMatrix$* { *; }
-keepclassmembers class biz.advancedcalendar.activities.accessories.InformationUnitMatrix** { *; }
-keepclassmembernames class biz.advancedcalendar.activities.accessories.InformationUnitMatrix$** { *; }

-keep class biz.advancedcalendar.views.accessories.InformationUnitSortOrdersHolder { *; }
-keep class biz.advancedcalendar.views.accessories.InformationUnitSortOrdersHolder$* { *; }
-keepclassmembers class biz.advancedcalendar.views.accessories.InformationUnitSortOrdersHolder** { *; }
-keepclassmembernames class biz.advancedcalendar.views.accessories.InformationUnitSortOrdersHolder$** { *; }

-keep class biz.advancedcalendar.views.accessories.InformationUnit { *; }
-keep class biz.advancedcalendar.views.accessories.InformationUnit$* { *; }
-keepclassmembers class biz.advancedcalendar.views.accessories.InformationUnit** { *; }
-keepclassmembernames class biz.advancedcalendar.views.accessories.InformationUnit$** { *; }

# -keep class biz.advancedcalendar.server.UserInfoViewModel**
# -keep class biz.advancedcalendar.server.UserInfoViewModel** { *; }

# -keep class biz.advancedcalendar.server.ErrorResponseModel**
# -keep class biz.advancedcalendar.server.ErrorResponseModel** { *; }

# -keep class biz.advancedcalendar.wsdl.sync.EntityListResponse**
# -keep class biz.advancedcalendar.wsdl.sync.EntityListResponse** { *; }

# -keep class biz.advancedcalendar.wsdl.sync.SetTaskResponse**
# -keep class biz.advancedcalendar.wsdl.sync.SetTaskResponse** { *; }

# -keep class biz.advancedcalendar.wsdl.sync.CreateUserResponse**
# -keep class biz.advancedcalendar.wsdl.sync.CreateUserResponse** { *; }

# -keep class biz.advancedcalendar.wsdl.sync.TasksListResponse**
# -keep class biz.advancedcalendar.wsdl.sync.TasksListResponse** { *; }

##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for Google Play services  ----------
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
##---------------End: proguard configuration for Google Play services  ----------
