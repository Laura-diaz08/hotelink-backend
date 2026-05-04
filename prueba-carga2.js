import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 50,
  duration: '1m',
  thresholds: {
    http_req_duration: ['avg < 1000'],
  },
};

const URL_BASE = 'http://localhost:8080';

export function setup() {
  const loginPayload = JSON.stringify({
    nombre: 'Maria',
    password: '2209',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(`${URL_BASE}/auth/login`, loginPayload, params);

  console.log('=== DEPURACIÓN DEL LOGIN ===');
  console.log('Status de respuesta del login:', res.status);
  console.log('Cuerpo de la respuesta del login:', res.body);
  console.log('============================');

  if (res.status !== 200) {
    console.error('El login falló con el código:', res.status);
    return { token: null };
  }

  const body = JSON.parse(res.body);
  return { token: body.token };
}

export default function (data) {
  const token = data.token;

  if (!token) {
    console.error('Se omitió la petición, no hay token de autenticación.');
    return;
  }

  // --- CONFIGURACIÓN DE HABITACIONES ---
  // IMPORTANTE: Asegúrate de que estos IDs existan en tu tabla de habitaciones.
  const habitacionesValidas = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]; 
  
  // Usamos el índice basado en __VU y __ITER para no repetir habitaciones en las peticiones concurrentes
  const habitacionIndex = (__VU + __ITER) % habitacionesValidas.length;
  const randomHabitacionId = habitacionesValidas[habitacionIndex];
  
  const randomHuespedes = Math.floor(Math.random() * 4) + 1;

  // Generación de fechas dinámicas sin colisiones usando el VU y la Iteración
  // Separamos cada iteración por 12 días para evitar solapamientos
  const diasInicio = (__VU * 365) + (__ITER * 12) + 1; 
  const fechaEntrada = new Date();
  fechaEntrada.setDate(fechaEntrada.getDate() + diasInicio);
  
  const diasDuracion = Math.floor(Math.random() * 5) + 1; // Duración de 1 a 5 días
  const fechaSalida = new Date(fechaEntrada);
  fechaSalida.setDate(fechaSalida.getDate() + diasDuracion);

  const strFechaEntrada = fechaEntrada.toISOString().split('T')[0];
  const strFechaSalida = fechaSalida.toISOString().split('T')[0];

  const payloadObj = {
    fechaEntrada: strFechaEntrada,
    fechaSalida: strFechaSalida,
    numeroHuespedes: randomHuespedes,
    habitacion: {
      id: randomHabitacionId
    }
  };

  const parametros = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  };

  const respuesta = http.post(`${URL_BASE}/reservas`, JSON.stringify(payloadObj), parametros);

  if (respuesta.status !== 200 && respuesta.status !== 201) {
    console.log(`Error HTTP ${respuesta.status} - Respuesta: ${respuesta.body}`);
  }

  check(respuesta, {
    'Creación de reserva exitosa': (r) => r.status === 200 || r.status === 201,
  });

  sleep(0.5);
}