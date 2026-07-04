// `sockjs-client` (dependencia transitiva del cliente STOMP) fue escrito asumiendo un entorno
// tipo Node y referencia el global `global`, que no existe en el navegador. Sin este shim, el
// bundle de producción de Angular (esbuild, sin polyfills de Node) falla en tiempo de ejecución
// con `ReferenceError: global is not defined` apenas se importa el adaptador de tiempo real.
(window as unknown as { global: unknown }).global = window;
