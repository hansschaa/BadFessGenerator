/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.badfessgenerator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 *
 * @author Hans
 */
public class BadFessGenerator {
    public static List<SokobanBoard> boards;
    public static List<SokobanBoard> randomBoards;
    public static List<SokobanBoard> solved;
    public static List<SokobanBoard> noSolved;
    public static int selectedID = 2;
    public static int numNewBoards = 2; 
    public static int randomMovesTimes = 2;
    private static final int[][] dirs = {
        {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}
    };
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String filePath = "mini2.txt"; 
        boards = new ArrayList<>();
        solved = new ArrayList<>();
        noSolved = new ArrayList<>();  
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder boardContent = new StringBuilder();
            int id = -1;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("ID: ")) {
                    
                    if (id != -1 && boardContent.length() > 0) {
                        SokobanBoard board = new SokobanBoard(id, boardContent.toString());
                        boards.add(board);
                        boardContent = new StringBuilder();
                    }
                
                    id = Integer.parseInt(line.substring(4).trim());
                } else {
                    boardContent.append(line).append("\n");
                }
            }

            if (id != -1 && boardContent.length() > 0) {
                SokobanBoard board = new SokobanBoard(id, boardContent.toString());
                boards.add(board);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0 ; i < boards.size(); i++){
            System.out.println("-----------------------------------");
            randomBoards = new ArrayList<>();
            
            //Select board
            SokobanBoard selectedBoard = boards.get(i);
            System.out.println("-> Selected Board");
            System.out.println(selectedBoard.getBoard());
            char[][] filledBoard = Fill(selectedBoard.getBoard());
            String filledBoardString = convertBoardToString(filledBoard);

            // Filled
            //System.out.println("-> Filled Board");
            //System.out.println(filledBoardString);

            //Generate board
            GenerateRandomBoards(filledBoardString);

            //Print random boards
            //PrintBoards(randomBoards);

            //Solve boards
            
            Execute();
            System.out.println("-----------------------------------");
            //Print Info
            /*System.out.println("Se resuelven");
            PrintBoards(solved);

            System.out.println("No se resuelven");
            PrintBoards(noSolved);*/
        }
        
        //Export to Excel
        ExportToExcel("results.xlsx");
    }
    
    private static void ExportToExcel(String filePath) throws IOException {
        
        System.out.println("-> Export to excel: " + filePath);
        
        Workbook workbook = new XSSFWorkbook();

        // Hoja para los niveles con solución
        Sheet sheet1 = workbook.createSheet("Niveles con Solución");

        // Definir encabezados de columna para niveles con solución
        Row headerRow1 = sheet1.createRow(0);
        headerRow1.createCell(0).setCellValue("Mapa");
        headerRow1.createCell(1).setCellValue("LURD");
        headerRow1.createCell(2).setCellValue("MOVES");
        headerRow1.createCell(3).setCellValue("PUSHES");
        
        int rowNumSheet1 = 1; // Empezar en la segunda fila después del encabezado
        for (SokobanBoard level : solved) {
            
            Row row = sheet1.createRow(rowNumSheet1++);
            Cell cell = row.createCell(0);
            cell.setCellValue(level.getBoard());
            
            cell = row.createCell(1);
            cell.setCellValue(level.message);
            
            cell = row.createCell(2);
            cell.setCellValue(level.GetMovements());
            
            cell = row.createCell(3);
            cell.setCellValue(level.GetPushes());
        }

        // Hoja para los niveles sin solución
        Sheet sheet2 = workbook.createSheet("Niveles sin Solución");
        // Definir encabezados de columna para niveles sin solución
        Row headerRow2 = sheet2.createRow(0);
        headerRow2.createCell(0).setCellValue("Mapa");
        headerRow2.createCell(1).setCellValue("Error");

        int rowNumSheet2 = 1; // Empezar en la segunda fila después del encabezado

        for (SokobanBoard level : noSolved) {
            Row row = sheet2.createRow(rowNumSheet2++);
            Cell cell = row.createCell(0);
            cell.setCellValue(level.getBoard());
            
            cell = row.createCell(1);
            cell.setCellValue(level.message);
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            System.out.println("-> Excel exported succesfully: " + filePath);
            workbook.write(fos);
        }
        catch(Exception e){
            System.out.println("Export Error: " + e.getMessage());
        }

        workbook.close();
    }
    
    public static String convertBoardToString(char[][] board) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                sb.append(board[i][j]);
            }
            // Opcionalmente, puedes agregar un salto de línea entre filas
            sb.append('\n');
        }

        return sb.toString();
    }
    
    
    public static void printBoard(char[][] board) {
        for (char[] row : board) {
            for (char cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }
    
    public static void PrintBoards(List<SokobanBoard> boards){
        for (SokobanBoard board : boards) {
            System.out.println("ID: " + board.getId());
            System.out.println(board.getBoard());
            System.out.println();
        }
    }

    private static void GenerateRandomBoards(String board) {
        SokobanBoard sokobanBoard = null;
        List<String> boardsCache = new ArrayList<String>();
        for (int i = 0; i < numNewBoards; i++) {
            //String newBoard = RandomlyMove(board, randomMovesTimes); 
            do{
                String newBoard = NearRandomlyMove(board, randomMovesTimes);
                sokobanBoard = new SokobanBoard(i + 1, newBoard);
            }while(boardsCache.contains(sokobanBoard.getBoard()));
            
            boardsCache.add(sokobanBoard.getBoard());
            randomBoards.add(sokobanBoard);
        }
    }
    
    public static void Execute(){
        for (SokobanBoard sokobanBoard : randomBoards) {
            File boardFile = null;
            try {
                // Crear un archivo específico para el tablero actual
                boardFile = new File("test", "board_" + sokobanBoard.getId() + ".txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(boardFile))) {
                    writer.write("ID: " + sokobanBoard.getId());
                    writer.newLine();
                    writer.write(sokobanBoard.getBoard());
                }

                // Llamar al .exe para cada tablero, pasando el archivo como argumento
                callExeForBoard(boardFile, sokobanBoard);

            } catch (IOException e) {
                System.err.println("Error creando o escribiendo en el archivo para el tablero: " + sokobanBoard);
                e.printStackTrace();
            } finally {
                // Eliminar el archivo si es necesario
                /*if (boardFile != null && boardFile.exists()) {
                    boardFile.delete();
                }*/
            }
        }
    }
    
    private static void callExeForBoard(File boardFile, SokobanBoard sokobanBoard) throws IOException {
        // Definir el comando y los argumentos necesarios
        String exePath = "fess.exe";

        // Crear el proceso
        ProcessBuilder processBuilder = new ProcessBuilder(exePath, boardFile.getAbsolutePath());
        List<String> command = processBuilder.command();
        StringBuilder commandString = new StringBuilder();
        for (String part : command) {
            commandString.append(part).append(" ");
        }
        System.out.println("Comando completo: " + commandString.toString().trim());
        
        // Iniciar el proceso
        Process process = processBuilder.start();

        // Leer la salida del proceso
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            //Checking
            if(line.contains("LURD")){
                System.out.println("SOLVED!");
                sokobanBoard.message = line.replace("LURD:", "");
                solved.add(sokobanBoard);
            }
            
            else if(line.contains("ERROR")){
                System.out.println("ERROR!");
                sokobanBoard.message =line;
                noSolved.add(sokobanBoard);
            }
            //System.out.println("Salida del .exe: " + line);
        }

        // Esperar a que el proceso termine
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Programa finalizado");
            } else {
                System.out.println("Hubo un error resolviendo el archivo " + boardFile.getName() + ". Código de salida: " + exitCode);
            }
        } catch (InterruptedException e) {
            System.err.println("El proceso fue interrumpido.");
            e.printStackTrace();
        }
    }
    
    public static void Export(){
        // Guardar los tableros en un archivo de texto
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("randomBoards.txt"))) {
            for (SokobanBoard sokobanBoard : randomBoards) {
                writer.write("ID: " + sokobanBoard.getId());
                writer.newLine();
                writer.write(sokobanBoard.getBoard());
                writer.newLine();
                writer.newLine(); // Línea en blanco para separar los tableros
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo en el archivo: " + e.getMessage());
        }
    }
    
    public static String NearRandomlyMove(String mBoard, int moves){
        Random random = new Random();
        String[] board = mBoard.split("\n");
        
        int rows = board.length;
        int cols = board[0].length();
        char[][] matrix = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            matrix[i] = board[i].toCharArray();
        }
        
        for (int i = 0; i < moves; i++) {
            List<Vec> boxandgoals = new ArrayList<>();
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    if (matrix[j][k] == '$' || matrix[j][k] == '.' || matrix[j][k] == '*') {
                        boxandgoals.add(new Vec(j,k, matrix[j][k]));
                    }
                }
            }
            
            List<Vec> emptyNeighboorsList = new ArrayList<>();
            Vec objectSpace = null;
            do{
                int randomIndex = random.nextInt(boxandgoals.size()); 
                objectSpace = boxandgoals.get(randomIndex);
                getEmptyNeighbors(objectSpace, matrix, emptyNeighboorsList);
                
            }while(emptyNeighboorsList.isEmpty());
            
            Vec emptySpace = emptyNeighboorsList.get(random.nextInt(0, emptyNeighboorsList.size()));
            
            switch (objectSpace.type) {
                case '*':
                    matrix[objectSpace.i][objectSpace.j] = '.';
                    matrix[emptySpace.i][emptySpace.j] = '$';
                    break;
                case '$':
                    matrix[objectSpace.i][objectSpace.j] = ' ';
                    matrix[emptySpace.i][emptySpace.j] = '$';
                    break;
                case '.':
                    matrix[objectSpace.i][objectSpace.j] = ' ';
                    matrix[emptySpace.i][emptySpace.j] = '.';
                    break;
                default:
                    System.out.println("No se conoce el elemento seleccionado para ser cmabiado");
                    break;
            }
        }
        
        String newBoard = convertBoardToString(matrix).replace('-', ' ');
        return newBoard;
    }
    
    private static void getEmptyNeighbors(Vec vec, char[][] matrix,List<Vec> emptyNeighboorsList) {
     

        for (int[] dir : dirs) {
            int newI = vec.i + dir[0];
            int newJ = vec.j + dir[1];
            if (isInBounds(new Vec(newI, newJ), matrix) && matrix[newI][newJ] == '-') {
                emptyNeighboorsList.add(new Vec(newI, newJ));
            }
        }
    }

    private static boolean isInBounds(Vec vec, char[][] matrix) {
        return vec.i >= 0 && vec.i < matrix.length && vec.j >= 0 && vec.j < matrix[0].length;
    }
    
    // Método para mover aleatoriamente las cajas o metas en el tablero
    public static String RandomlyMove(String mBoard, int moves) {
        Random random = new Random();
        String[] board = mBoard.split("\n");
        
        int rows = board.length;
        int cols = board[0].length();
        char[][] matrix = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            matrix[i] = board[i].toCharArray();
        }

        
        for (int i = 0; i < moves; i++) {
            List<Vec> emptySpaces = new ArrayList<>();
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    if (matrix[j][k] == '-') {
                        emptySpaces.add(new Vec(j,k));
                    }
                }
            }

            if (emptySpaces.isEmpty()) {
                break; 
            }

            int randomIndex = random.nextInt(emptySpaces.size());
            Vec emptyIndex = emptySpaces.get(randomIndex);
            
            Vec objectIndex = new Vec(random.nextInt(rows), random.nextInt(cols));
            while (matrix[objectIndex.i][objectIndex.j] == '#' || matrix[objectIndex.i][objectIndex.j] == '-' || matrix[objectIndex.i][objectIndex.j] == ' ') {
                objectIndex = new Vec(random.nextInt(rows), random.nextInt(cols));
            }

            switch (matrix[objectIndex.i][objectIndex.j]) {
                case '+':
                    matrix[objectIndex.i][objectIndex.j] = '.';
                    matrix[emptyIndex.i][emptyIndex.j] = '@';
                    break;
                case '*':
                    matrix[objectIndex.i][objectIndex.j] = '.';
                    matrix[emptyIndex.i][emptyIndex.j] = '$';
                    break;
                case '@':
                    matrix[objectIndex.i][objectIndex.j] = ' ';
                    matrix[emptyIndex.i][emptyIndex.j] = '@';
                    break;
                case '.':
                    matrix[objectIndex.i][objectIndex.j] = ' ';
                    matrix[emptyIndex.i][emptyIndex.j] = '.';
                    break;
                case '$':
                    matrix[objectIndex.i][objectIndex.j] = ' ';
                    matrix[emptyIndex.i][emptyIndex.j] = '$';
                    break;
                default:
                    System.out.println("No se conoce el elemento seleccionado para ser cmabiado");
                    break;
            }
        }

        String newBoard = convertBoardToString(matrix).replace('-', ' ');
        return newBoard;
    }
    
    public static char[][] Fill(String mBoard) {
        
        String[] board = mBoard.split("\n");
        
        int rows = board.length;
        int cols = board[0].length();
        char[][] matrix = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            matrix[i] = board[i].toCharArray();
        }

        Vec playerPos = GetPlayerPos(matrix, '@');
        if(playerPos == null)
            playerPos = GetPlayerPos(matrix, '+');
        
        FloodFill(matrix, playerPos.i, playerPos.j);

        return matrix;
    }

    public static void FloodFill(char[][] matrix, int i, int j) {

        int rows = matrix.length;
        int cols = matrix[0].length;

        // Validate indices
        if (i < 0 || i >= rows || j < 0 || j >= cols) {
            return;
        }

        // If the current cell is not the target, return
        if (matrix[i][j] == '#' || matrix[i][j] == '-') {
            return;
        }

        char oldChar =  matrix[i][j];
        matrix[i][j] = '-';

        FloodFill(matrix, i, j-1); // left
        FloodFill(matrix, i, j+1); // right
        FloodFill(matrix, i-1, j); // Up
        FloodFill(matrix, i+1, j); // Down
        
        if(oldChar!=' ')
            matrix[i][j] = oldChar;
    }

    private static Vec GetPlayerPos(char[][] matrix, char character) {
        
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == character) {
                    return new Vec(i,j);
                }
            }
        }
        return null; // Character not found
    }
    
    public static int GetIDCount(String filePath){
        String searchString = "ID";
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int index = 0;
                if(line.contains(searchString))
                    count++;
            }
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
        }

        System.out.println("La cadena \"" + searchString + "\" aparece " + count + " veces en el archivo " + filePath);
        
        return count;
    }
}
    


