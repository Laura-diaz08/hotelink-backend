const url = "http://localhost:8080/auth/login";
const body = JSON.stringify({ nombre: "admin", password: "123" }); // Tu usuario de prueba
const cabeceras = { "Content-Type": "application/json" };

const NUM_PETICIONES = 40; // Número de usuarios intentando entrar a la vez
let exitosas = 0;
let errores = 0;
let tiempos = [];

console.log(`Lanzando ${NUM_PETICIONES} peticiones a la vez contra el servidor...`);

const promesas = [];
for (let i = 0; i < NUM_PETICIONES; i++) {
    const inicioPeticion = Date.now();
    
    // Hacemos la petición HTTP
    const peticion = fetch(url, { method: "POST", body: body, headers: cabeceras })
        .then(res => {
            tiempos.push(Date.now() - inicioPeticion);
            if (res.ok) exitosas++;
            else errores++;
        })
        .catch(() => errores++);
        
    promesas.push(peticion);
}

// Cuando todas las peticiones terminen, calculamos los resultados
Promise.all(promesas).then(() => {
    const tiempoMedio = tiempos.reduce((a, b) => a + b, 0) / tiempos.length;
    
    console.log("\n=== RESULTADOS DE LA PRUEBA ===");
    console.log(`Número de peticiones: ${NUM_PETICIONES}`);
    console.log(`Tiempo de respuesta medio: ${tiempoMedio.toFixed(2)} ms`);
    console.log(`Errores HTTP: ${errores}`);
    
    if (errores === 0) {
        console.log("\nConclusión: ¡El servidor aguanta perfectamente esta carga!");
    } else {
        console.log("\nConclusión: El servidor ha empezado a fallar y a rechazar peticiones.");
    }
});