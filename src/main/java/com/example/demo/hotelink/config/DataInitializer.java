package com.example.demo.hotelink.config; // Asegúrate de que el paquete sea el correcto

import com.example.demo.hotelink.model.Articulo;
import com.example.demo.hotelink.model.Servicio;
import com.example.demo.hotelink.repository.ArticuloRepository;
import com.example.demo.hotelink.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. INICIALIZAR LOS SERVICIOS (Instalaciones del hotel)
        if (servicioRepository.count() == 0) {
            System.out.println("Cargando servicios por defecto...");

            Servicio spa = new Servicio();
            spa.setNombre("Circuito Spa");
            spa.setDescripcion("Acceso de 90 minutos a las piscinas termales, sauna y baño turco.");
            spa.setPrecio(25.0);
            spa.setAforoMaximo(15);

            Servicio gimnasio = new Servicio();
            gimnasio.setNombre("Gimnasio");
            gimnasio.setDescripcion("Acceso libre a la sala de máquinas y pesas.");
            gimnasio.setPrecio(0.0); // Es gratis para los clientes
            gimnasio.setAforoMaximo(20);

            Servicio masaje = new Servicio();
            masaje.setNombre("Masaje Relajante");
            masaje.setDescripcion("Masaje de cuerpo entero con aceites esenciales (45 min).");
            masaje.setPrecio(50.0);
            masaje.setAforoMaximo(1);

            servicioRepository.saveAll(Arrays.asList(spa, gimnasio, masaje));
            System.out.println("Servicios cargados con éxito.");
        }

        // 2. INICIALIZAR LOS ARTÍCULOS (Consumibles y Extras)
        if (articuloRepository.count() == 0) {
            System.out.println("Cargando artículos por defecto...");

            Articulo cocaCola = new Articulo();
            cocaCola.setNombre("Coca-Cola");
            cocaCola.setCategoria("MINIBAR");
            cocaCola.setPrecio(3.50);

            Articulo agua = new Articulo();
            agua.setNombre("Botella de Agua");
            agua.setCategoria("MINIBAR");
            agua.setPrecio(2.00);

            Articulo toalla = new Articulo();
            toalla.setNombre("Toalla Extra");
            toalla.setCategoria("PETICION_HABITACION");
            toalla.setPrecio(0.00); // Petición gratuita

            Articulo desayuno = new Articulo();
            desayuno.setNombre("Desayuno Buffet");
            desayuno.setCategoria("RESTAURANTE");
            desayuno.setPrecio(15.00);

            articuloRepository.saveAll(Arrays.asList(cocaCola, agua, toalla, desayuno));
            System.out.println("Artículos cargados con éxito.");
        }
    }
}