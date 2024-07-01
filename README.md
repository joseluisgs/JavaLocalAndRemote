# Kotlin Local and Remote

![Kotlin](./images/java.png)

## Acerca de

Este proyecto es un ejemplo de como crear un servicio para el almacenamiento de datos Java.

Tendremos un repositorio local usando una base de datos, y un repositorio remoto utilizando una API REST.

Ambos repositorios estarán encapsulados en un solo servicio, que será el que utilicemos en nuestra aplicación.
Es decir, cada cierto tiempo se actualizará la base de datos local con los datos de la API REST. Otro caso de uso es que
no haya conexión, o simplemente
queramos trabajar con los datos locales o si no se encuentran actualizados, con los datos de la API REST.
Además, otra caché en memoria se encargará de almacenar los últimos datos obtenidos de la base de datos local o de la
API REST para mejorar el rendimiento de la aplicación. Es decir, si la base de datos local no está actualizada, se
obtendrán los datos de la API REST cada cierto intervalo de refresco y se almacenarán en la base de datos local. En la
caché
en memoria se almacenarán los últimos datos obtenidos de la base de datos local o de la API REST.

El intervalo de refresco de la base de datos local y el de la caché en memoria se puede configurar.

Por otro lado, el servicio podrá importar/exportar datos en CSV y JSON.

Finalmente, tendremos un servicio de notificaciones, que nos permitirá recibir información de los cambios realizados.

El objetivo docente es mostrar implementaciones asíncronas y reactiva en el procesamiento de la información.

Puedes seguir el proyecto en [GitHub](https://github.com/joseluisgs/JavaLocalAndRemote) y en los commits indicados.

```
El proyecto está sobre-exagerado para mostrar diferentes técnicas y no es un ejemplo de cómo hacer una aplicación real.
Se trata de elementos aislados que se pueden utilizar en una aplicación real con el objetivo que el alumnado identifique
y asimile nuevas técnicas y herramientas de programación. No tiene que ser la mejor forma ni la más eficiente, pero sí
intenta acercar otras formas de programar nuevas que se están viendo en clase. El objetivo es aprender y no hacer una
aplicación real y eficiente. 
```