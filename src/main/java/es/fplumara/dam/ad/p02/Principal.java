package es.fplumara.dam.ad.p02;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

/**
 * P02. Organizador de capturas y clips de videojuegos.
 * Trabaja con rutas (Path) y con la clase Files.
 */
public class Principal {
    
    // Rutas relativas a la carpeta del proyecto (Working directory)
    private static final Path DESCARGAS = Path.of("datos", "descargas");
    private static final Path ORGANIZADO = Path.of("datos", "organizado");

    public static void main(String[] args) {
        // Paso 1
        mostrarRutas();

        // Paso 2
        System.out.println();
        comprobar(DESCARGAS);
        comprobar(DESCARGAS.resolve("leeme.txt"));
        comprobar(Path.of("datos", "fotos"));

        try {
            // Paso 3
            System.out.println();
            listar(DESCARGAS);

            // Paso 4
            System.out.println();
            mostrarAtributos(DESCARGAS.resolve("astrofaro_2026-03-14_2215.png"));
            mostrarAtributos(DESCARGAS.resolve("pixelhuerto_2026-05-02_1030.png"));
            mostrarAtributos(DESCARGAS.resolve("viejas"));

            // Paso 5
            System.out.println();
            organizar();

            // Paso 6
            System.out.println();
            moverAPapelera();

            // Paso 7
            System.out.println();
            recorrer(DESCARGAS);

            // Paso 8
            System.out.println();
            borrar();

        } catch (IOException e) {
            System.out.println("Error de entrada/salida: " + e);
        }
    }

    // Paso 1. Partes de una ruta
    private static void mostrarRutas() {
        System.out.println("Ruta relativa: " + DESCARGAS);
        System.out.println("Ruta absoluta: " + DESCARGAS.toAbsolutePath());
        System.out.println("Nombre: " + DESCARGAS.getFileName());
        System.out.println("Carpeta padre: " + DESCARGAS.getParent());
        Path leeme = DESCARGAS.resolve("leeme.txt");
        System.out.println("Ruta de leeme.txt: " + leeme);
    }

    // Paso 2. Comprobar qué hay en una ruta
    private static void comprobar(Path ruta) {
        System.out.println(ruta + " -> existe: " + Files.exists(ruta)
                + ", directorio: " + Files.isDirectory(ruta)
                + ", fichero: " + Files.isRegularFile(ruta)
                + ", legible: " + Files.isReadable(ruta));
    }

    // Paso 3. Listar el contenido de una carpeta (sin entrar en subcarpetas)
    private static void listar(Path carpeta) throws IOException {
        System.out.println("Contenido de " + carpeta + ":");
        int total = 0;
        try (Stream<Path> flujo = Files.list(carpeta)) {
            List<Path> rutas = flujo.sorted().toList();
            for (Path ruta : rutas) {
                if (Files.isDirectory(ruta)) {
                    System.out.println(" [DIR] " + ruta.getFileName());
                } else {
                    System.out.println(" [FIC] " + ruta.getFileName());
                }
                total++;
            }
        }
        System.out.println("Total: " + total + " elementos");
    }

    // Paso 4. Leer los atributos de un fichero o carpeta
    private static void mostrarAtributos(Path ruta) throws IOException {
        System.out.println("Atributos de " + ruta.getFileName() + ":");
        System.out.println(" Tamaño: " + Files.size(ruta) + " bytes");
        System.out.println(" Última modificación: " + Files.getLastModifiedTime(ruta));
        BasicFileAttributes atributos = Files.readAttributes(ruta, BasicFileAttributes.class);
        System.out.println(" Es carpeta: " + atributos.isDirectory());
        System.out.println(" Creado: " + atributos.creationTime());
    }

    // Paso 5. Crear carpetas por tipo y mes, y copiar cada fichero a la suya
    private static void organizar() throws IOException {
        System.out.println("Organizando " + DESCARGAS + "...");
        int copiados = 0;
        try (Stream<Path> flujo = Files.list(DESCARGAS)) {
            List<Path> rutas = flujo.sorted().toList();
            for (Path ruta : rutas) {
                String nombre = ruta.getFileName().toString();
                String minusculas = nombre.toLowerCase();
                String tipo;
                
                if (minusculas.endsWith(".png")) {
                    tipo = "capturas";
                } else if (minusculas.endsWith(".mp4")) {
                    tipo = "clips";
                } else {
                    continue; // leeme.txt y las carpetas no se organizan
                }

                // El nombre es juego_aaaa-mm-dd_hhmm: el mes son los 7 caracteres tras el primer "_"
                int posicion = nombre.indexOf('_');
                String mes = nombre.substring(posicion + 1, posicion + 8);
                Path carpetaDestino = ORGANIZADO.resolve(tipo).resolve(mes);
                
                Files.createDirectories(carpetaDestino);
                Path destino = carpetaDestino.resolve(nombre);
                Files.copy(ruta, destino, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("" + nombre + " -> " + carpetaDestino);
                copiados++;
            }
        }
        System.out.println("Ficheros copiados: " + copiados);
    }

    // Paso 6. Mover la captura vacía a una papelera
    private static void moverAPapelera() throws IOException {
        Path papelera = ORGANIZADO.resolve("papelera");
        Files.createDirectories(papelera);
        Path vacia = ORGANIZADO.resolve("capturas").resolve("2026-05").resolve("pixelhuerto_2026-05-02_1030.png");
        Path destino = papelera.resolve(vacia.getFileName());
        Files.move(vacia, destino, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Movido: " + vacia);
        System.out.println(" a: " + destino);
    }

    // Paso 7. Recorrer una carpeta y todas sus subcarpetas
    private static void recorrer(Path carpeta) throws IOException {
        System.out.println("Recorrido de " + carpeta + ":");
        int ficheros = 0;
        int carpetas = 0;
        long bytes = 0;
        
        try (Stream <Path> flujo = Files.walk(carpeta)) {
            List<Path> rutas = flujo.sorted().toList();
            for (Path ruta : rutas) {
                if (Files.isDirectory(ruta)) {
                    carpetas++;
                } else {
                    ficheros++;
                    bytes = bytes + Files.size(ruta);
                    if (ruta.getNameCount() > carpeta.getNameCount() + 1) {
                        // Solo mostramos lo que está dentro de subcarpetas
                        System.out.println(" " + carpeta.relativize(ruta));
                    }
                }
            }
        }
        System.out.println("Carpetas (incluida la inicial): " + carpetas);
        System.out.println("Ficheros: " + ficheros);
        System.out.println("Tamaño total: " + bytes / 1024 + " KB");
    }

    // Paso 8. Borrar y tratar cada excepción por separado
    private static void borrar() {
        Path papelera = ORGANIZADO.resolve("papelera");
        Path vacia = papelera.resolve("pixelhuerto_2026-05-02_1030.png");
        Path fotos = Path.of("datos", "fotos");
        Path[] aBorrar = {vacia, papelera, ORGANIZADO.resolve("capturas"), fotos};
        
        for (int i = 0; i < aBorrar.length; i++) {
            try {
                Files.delete(aBorrar[i]);
                System.out.println("Borrado: " + aBorrar[i]);
            } catch (NoSuchFileException e) {
                System.out.println("No existe: " + e.getFile());
            } catch (DirectoryNotEmptyException e) {
                System.out.println("La carpeta no está vacía: " + e.getFile());
            } catch (IOException e) {
                System.out.println("Error al borrar " + aBorrar[i] + ": " + e);
            }
        }
        
        try {
            boolean borrado = Files.deleteIfExists(fotos);
            System.out.println("deleteIfExists(" + fotos + "): " + borrado);
            Files.createDirectory(ORGANIZADO);
        } catch (FileAlreadyExistsException e) {
            System.out.println("Ya existe: " + e.getFile());
        } catch (IOException e) {
            System.out.println("Error: " + e);

    }
}