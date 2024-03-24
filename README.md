# netbeans-svuid-generator

Generator of serialVersionUID for classes that implement java.io.Serializable.

With this plugin installed two options will appear when the "serialVersionUID 
not defined" warning is shown:

* Add default serialVersionUID (there will be two, the original Java hint warning will not disappear)
* Add generated serialVersionUID

Choose the first one to generate a serialVersionUID value of 1L. Choose the 
second option to generate a hashed  
