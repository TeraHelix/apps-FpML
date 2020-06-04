# Building and Running TeraHelix FpML Application

This guide details how you can build this repository locally in order to run the examples, inspect the code or to contribute back improvements and fixes. 

## Prequisites

You need to install the following in order to be able to build this project:

* **Git** - as you are already a GitHub user, you probably already have this setup. If not, please consult they [GitHub Getting Started Guide](https://help.github.com/en/github/getting-started-with-github/set-up-git). It is assumed that you have already cloned the project to a local directory.
* **Java 11** - You can obtain Java 11 from [Adopt OpenJDK](https://adoptopenjdk.net/). Alternatively, an excellent production distribution is also maintained by Amazon - [Corretto 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/what-is-corretto-11.html)  
* **Apache Maven** - Obtain the latest version of Apache Maven from [https://maven.apache.org/](https://maven.apache.org/) 

### 'terahelix-licensee' Token

This project makes use of the TeraHelix [Spear](https://www.terahelix.io/products-spear/index.html) and [Platform](https://www.terahelix.io/products-platform/index.html) binaries. 

You need to obtain a 'terahelix-licensee' token (used in the configuration below) in order to get access to these binaries. Please contact [admin@terahelix.io](mailto:admin@terahelix.io) to obtain your token.

### Configuring your Maven `settings.xml` file 

Once you have installed Apache Maven, you need to configure its `settings.xml` file such that your environment is able to resolve the TeraHelix Spear and Platform binaries that is required for the build.

In particular, you need to update the `repositories`, `pluginRepositories` and `servers` section of the `settings.xml` file.

You can either use this projects version in its entirety - [.github/workflows/settings.xml](.github/workflows/settings.xml) - or simply just this an example of what to update your `settings.xml` file toif you want to preserve other modifications.

However, please ensure that you replace these sections: 

```
<username>${env.USERNAME_PACKAGE_READ_WRITE_DELETE}</username>
<password>${env.TOKEN_PACKAGE_READ_WRITE_DELETE}</password>
```

with

```
<username>terahelix-licensee</username>
<password>##YourToken##</password>
```

Where `##YourToken##` is the token you obtain by contacting [admin@terahelix.io](mailto:admin@terahelix.io).

## Building the Project

You are now ready to build the project. You can do a full build by executing the following command :

```
mvn clean install
```

Or if you prefer, you can do so without executing the tests:

```
mvn clean install -DskipTests=true
```
