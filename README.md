# netbeans-svuid-generator

Generator of serialVersionUID for classes that implement java.io.Serializable.

With this plugin installed two options will appear when the "serialVersionUID 
not defined" warning is shown:

* Add default serialVersionUID (custom)
* Add generated serialVersionUID (custom)

Choose the first one to generate a serialVersionUID value of 1L. Choose the 
second option to generate a hashed.

There will be two 'Add default serialVersionUID' warnings, the original - 
without '(custom)' - is not removed.
