import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.aiquizgenerator"
    compileSdk = 34

    packagingOptions {
        resources {
            pickFirst("META-INF/DEPENDENCIES")
            pickFirst("META-INF/LICENSE")
            pickFirst("META-INF/LICENSE.txt")
            pickFirst("META-INF/license.txt")
            pickFirst("META-INF/NOTICE")
            pickFirst("META-INF/NOTICE.txt")
            pickFirst("META-INF/notice.txt")
            pickFirst("META-INF/ASL2.0")
            pickFirst("META-INF/INDEX.LIST")
            pickFirst("versionchanges.txt")
            // Add any other duplicate files here if needed
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets") // Defines the assets folder
        }
    }

    defaultConfig {
        applicationId = "com.example.aiquizgenerator"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

configurations.all {
    exclude(group = "commons-logging", module = "commons-logging")
    exclude(group = "log4j", module = "log4j")
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
        implementation("org.apache.poi:poi:4.1.2")
        implementation("org.apache.poi:poi-ooxml:4.1.2")
        implementation("org.apache.poi:poi-scratchpad:4.1.2")
        implementation("org.apache.poi:poi-ooxml-schemas:4.1.2")
    implementation("net.sourceforge.tess4j:tess4j:4.5.1")
    implementation("org.slf4j:jcl-over-slf4j:1.7.30") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.slf4j:log4j-over-slf4j:1.7.30") {
        exclude(group = "log4j", module = "log4j")
    }
    implementation("com.rmtheis:tess-two:9.1.0")
    implementation("com.itextpdf:itextg:5.5.10")
    implementation("junit:junit:4.+")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.google.code.gson:gson:2.10")
    implementation ("org.apache.opennlp:opennlp-tools:2.2.0")
    implementation ("cc.mallet:mallet:2.0.8")
    implementation ("org.apache.commons:commons-math3:3.6.1")
    implementation ("net.sf.extjwnl:extjwnl:2.0.2")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("org.json:json:20210307")
}