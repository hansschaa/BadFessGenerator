/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.badfessgenerator;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Hans
 */
// Clase para representar un tablero de Sokoban con su ID
class SokobanBoard {
    private int id;
    private String board;
    public String message;

    public SokobanBoard(int id, String board) {
        this.id = id;
        this.board = board;
    }

    public int getId() {
        return id;
    }

    public String getBoard() {
        return board;
    }

    void Clean() {
        board.replace('-', ' ');
    }
}