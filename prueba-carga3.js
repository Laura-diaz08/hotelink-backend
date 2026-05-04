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

  // Generación de fechas únicas y secuenciales
  const diasInicio = 10 + (__VU * 200) + (__ITER * 10);
  const fechaInicioObj = new Date();
  fechaInicioObj.setDate(fechaInicioObj.getDate() + diasInicio);

  const diasDuracion = Math.floor(Math.random() * 5) + 1; // Duración de 1 a 5 días
  const fechaFinObj = new Date(fechaInicioObj);
  fechaFinObj.setDate(fechaFinObj.getDate() + diasDuracion);

  const strFechaInicio = fechaInicioObj.toISOString().split('T')[0];
  const strFechaFin = fechaFinObj.toISOString().split('T')[0];

  // Consultar habitaciones disponibles
  const urlDisponibles = `${URL_BASE}/habitaciones/disponibles?inicio=${strFechaInicio}&fin=${strFechaFin}`;
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  };

  const resDisponibles = http.get(urlDisponibles, params);

  if (resDisponibles.status !== 200) {
    console.log(`[VU=${__VU}, Iter=${__ITER}] Error consultando disponibilidad (${resDisponibles.status}): ${urlDisponibles}`);
    return;
  }

  let habitacionesDisponibles = [];
  try {
    habitacionesDisponibles = JSON.parse(resDisponibles.body);
  } catch (e) {
    console.log(`Error al parsear disponibilidad: ${e}`);
    return;
  }

  if (habitacionesDisponibles.length === 0) {
    console.log(`[VU=${__VU}, Iter=${__ITER}] No hay habitaciones disponibles del ${strFechaInicio} al ${strFechaFin}`);
    return;
  }

  // Seleccionar aleatoriamente una habitación de la lista
  const randomIndex = Math.floor(Math.random() * habitacionesDisponibles.length);
  const habitacionElegida = habitacionesDisponibles[randomIndex];
  const randomHuespedes = Math.floor(Math.random() * 4) + 1;

  const payloadObj = {
    clienteId: 2, 
    fechaInicio: strFechaInicio,
    fechaFin: strFechaFin,
    numeroHuespedes: randomHuespedes
  };

  // Realizar reserva a la ruta correcta
  const resReserva = http.post(`${URL_BASE}/habitaciones/${habitacionElegida.id}/reservar`, JSON.stringify(payloadObj), params);

  if (resReserva.status !== 200 && resReserva.status !== 201) {
    console.log(`[VU=${__VU}, Iter=${__ITER}] Error al reservar HTTP ${resReserva.status} - Respuesta: ${resReserva.body}`);
  } else {
    console.log(`[VU=${__VU}, Iter=${__ITER}] Reserva creada con éxito para la habitación ${habitacionElegida.id}`);
  }

  check(resReserva, {
    'Creación de reserva exitosa': (r) => r.status === 200 || r.status === 201,
  });

  sleep(0.5);
}