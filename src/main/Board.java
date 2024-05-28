package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Board extends JPanel {

	private final int NUM_IMAGES = 13; // Số lượng ảnh
	private final int CELL_SIZE = 15; // Kích thước của mỗi ô

	private final int COVER_FOR_CELL = 10; // Giá trị ô bị che
	private final int MARK_FOR_CELL = 10; // Giá trị ô được đánh dấu
	private final int EMPTY_CELL = 0; // Giá trị ô trống
	private final int MINE_CELL = 9; // Giá trị ô có mìn
	private final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL; // Ô có mìn bị che
	private final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL; // Ô có mìn được đánh dấu

	private final int DRAW_MINE = 9; // Vẽ mìn
	private final int DRAW_COVER = 10; // Vẽ ô che
	private final int DRAW_MARK = 11; // Vẽ dấu hiệu
	private final int DRAW_WRONG_MARK = 12; // Vẽ dấu hiệu sai

	private final int N_MINES = 40; // Số lượng mìn
	private final int N_ROWS = 16; // Số hàng
	private final int N_COLS = 16; // Số cột

	private final int BOARD_WIDTH = N_COLS * CELL_SIZE + 1; // Chiều rộng bảng
	private final int BOARD_HEIGHT = N_ROWS * CELL_SIZE + 1; // Chiều cao bảng

	private int[] field; // Mảng lưu trạng thái của các ô
	private boolean inGame; // Biến xác định trạng thái trò chơi (đang chơi hay kết thúc)
	private int minesLeft; // Số mìn còn lại chưa được đánh dấu
	private Image[] img; // Mảng chứa các hình ảnh

	private int allCells; // Tổng số ô
	private final JLabel statusbar; // Thanh trạng thái hiển thị thông tin trò chơi

	// Hàm khởi tạo Board
	public Board(JLabel statusbar) {
		this.statusbar = statusbar; // Gán thanh trạng thái
		initBoard(); // Khởi tạo bảng
	}

	// Phương thức khởi tạo bảng
	private void initBoard() {
		// Đặt kích thước ưu tiên cho bảng
		setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));

		// Tạo mảng hình ảnh
		img = new Image[NUM_IMAGES];
		for (int i = 0; i < NUM_IMAGES; i++) {
			var path = "src/resources/" + i + ".png"; // Đường dẫn đến hình ảnh
			img[i] = (new ImageIcon(path)).getImage(); // Tải hình ảnh
		}

		// Thêm bộ lắng nghe sự kiện chuột
		addMouseListener(new MinesAdapter());
		newGame(); // Bắt đầu trò chơi mới
	}

	// Phương thức bắt đầu trò chơi mới
	private void newGame() {
		int cell;
		var random = new Random(); // Tạo đối tượng random
		inGame = true; // Đặt trạng thái trò chơi là đang chơi
		minesLeft = N_MINES; // Đặt số mìn còn lại

		allCells = N_ROWS * N_COLS; // Tính tổng số ô
		field = new int[allCells]; // Khởi tạo mảng trạng thái các ô

		for (int i = 0; i < allCells; i++) {
			field[i] = COVER_FOR_CELL; // Che tất cả các ô
		}

		statusbar.setText(Integer.toString(minesLeft)); // Cập nhật thanh trạng thái

		int i = 0;
		while (i < N_MINES) {
			int position = (int) (allCells * random.nextDouble()); // Chọn vị trí ngẫu nhiên

			if ((position < allCells) && (field[position] != COVERED_MINE_CELL)) {
				int current_col = position % N_COLS;
				field[position] = COVERED_MINE_CELL; // Đặt mìn tại vị trí
				i++;

				// Tăng giá trị ô xung quanh ô có mìn
				if (current_col > 0) {
					cell = position - 1 - N_COLS;
					if (cell >= 0 && field[cell] != COVERED_MINE_CELL)
						field[cell] += 1;
					cell = position - 1;
					if (cell >= 0 && field[cell] != COVERED_MINE_CELL)
						field[cell] += 1;
					cell = position + N_COLS - 1;
					if (cell < allCells && field[cell] != COVERED_MINE_CELL)
						field[cell] += 1;
				}
				cell = position - N_COLS;
				if (cell >= 0 && field[cell] != COVERED_MINE_CELL)
					field[cell] += 1;
				cell = position + N_COLS;
				if (cell < allCells && field[cell] != COVERED_MINE_CELL)
					field[cell] += 1;

				if (current_col < (N_COLS - 1)) {
					cell = position - N_COLS + 1;
					if (cell >= 0 && field[cell] != COVERED_MINE_CELL)
						field[cell] += 1;
					cell = position + N_COLS + 1;
					if (cell < allCells && field[cell] != COVERED_MINE_CELL)
						field[cell] += 1;
					cell = position + 1;
					if (cell < allCells && field[cell] != COVERED_MINE_CELL)
						field[cell] += 1;
				}
			}
		}
	}

	// Phương thức tìm các ô trống liền kề
	private void find_empty_cells(int j) {
		int current_col = j % N_COLS;
		int cell;
	/* 
	 * kiểm tra các ô nằm ở phía trên và bên trái của ô hiện tại, 
	 * và nếu các ô đó chưa được mở và không chứa mìn, sẽ mở các ô đó và tiếp tục tìm kiếm các ô trống liên tiếp.	
	 */
		if (current_col > 0) {
			cell = j - N_COLS - 1;
			if (cell >= 0 && field[cell] > MINE_CELL) {
				field[cell] -= COVER_FOR_CELL;
				if (field[cell] == EMPTY_CELL)
					find_empty_cells(cell);
			}
			cell = j - 1;
			if (cell >= 0 && field[cell] > MINE_CELL) {
				field[cell] -= COVER_FOR_CELL;
				if (field[cell] == EMPTY_CELL)
					find_empty_cells(cell);
			}
			cell = j + N_COLS - 1;
			if (cell < allCells && field[cell] > MINE_CELL) {
				field[cell] -= COVER_FOR_CELL;
				if (field[cell] == EMPTY_CELL)
					find_empty_cells(cell);
			}
		}
	//kiểm tra các ô nằm ngay trên và ngay dưới ô hiện tại, và thực hiện hành động tương tự như trên.
		cell = j - N_COLS;
		if (cell >= 0 && field[cell] > MINE_CELL) {
			field[cell] -= COVER_FOR_CELL;
			if (field[cell] == EMPTY_CELL)
				find_empty_cells(cell);
		}
		cell = j + N_COLS;
		if (cell < allCells && field[cell] > MINE_CELL) {
			field[cell] -= COVER_FOR_CELL;
			if (field[cell] == EMPTY_CELL)
				find_empty_cells(cell);
		}
	//kiểm tra các ô nằm ở phía dưới và bên phải của ô hiện tại, và thực hiện hành động tương tự.
		if (current_col < (N_COLS - 1)) {
			cell = j - N_COLS + 1;
			if (cell >= 0 && field[cell] > MINE_CELL) {
				field[cell] -= COVER_FOR_CELL;
				if (field[cell] == EMPTY_CELL)
					find_empty_cells(cell);
			}
			cell = j + N_COLS + 1;
			if (cell < allCells && field[cell] > MINE_CELL) {
				field[cell] -= COVER_FOR_CELL;
				if (field[cell] == EMPTY_CELL)
					find_empty_cells(cell);
			}
			cell = j + 1;
			if (cell < allCells && field[cell] > MINE_CELL) {
				field[cell] -= COVER_FOR_CELL;
				if (field[cell] == EMPTY_CELL)
					find_empty_cells(cell);
			}
		}
	}

	// Phương thức vẽ bảng Minesweeper
	@Override
	public void paintComponent(Graphics g) {
		int uncover = 0;

		for (int i = 0; i < N_ROWS; i++) {
			for (int j = 0; j < N_COLS; j++) {
				int cell = field[(i * N_COLS) + j];

				if (inGame && cell == MINE_CELL) {
					inGame = false;
				}

				if (!inGame) {
					if (cell == COVERED_MINE_CELL) {
						cell = DRAW_MINE;
					} else if (cell == MARKED_MINE_CELL) {
						cell = DRAW_MARK;
					} else if (cell > COVERED_MINE_CELL) {
						cell = DRAW_WRONG_MARK;
					} else if (cell > MINE_CELL) {
						cell = DRAW_COVER;
					}
				} else {
					if (cell > COVERED_MINE_CELL) {
						cell = DRAW_MARK;
					} else if (cell > MINE_CELL) {
						cell = DRAW_COVER;
						uncover++;
					}
				}

				g.drawImage(img[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
			}
		}

		if (uncover == 0 && inGame) {
			inGame = false;
			statusbar.setText("Game won");
		} else if (!inGame) {
			statusbar.setText("Game lost");
		}
	}

	// Lớp nội MinesAdapter để xử lý sự kiện chuột
	private class MinesAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			int cCol = x / CELL_SIZE;
			int cRow = y / CELL_SIZE;

			boolean doRepaint = false;

			if (!inGame) {
				newGame();
				repaint();
			}
		//USE CASE ĐẶT CỜ
			if ((x < N_COLS * CELL_SIZE) && (y < N_ROWS * CELL_SIZE)) {
				if (e.getButton() == MouseEvent.BUTTON3) { // Nhấn chuột phải để đánh dấu (cờ)
					if (field[(cRow * N_COLS) + cCol] > MINE_CELL) { // Kiểm tra nếu ô hiện tại đang được che phủ (không phải ô đã mở)
/*
 * field là một mảng một chiều (kiểu int[]) đại diện cho tất cả các ô trên bảng Minesweeper. 
    Mỗi phần tử trong mảng này lưu trữ trạng thái của một ô trên bảng.
 * Bảng Minesweeper có kích thước N_ROWS x N_COLS. 
   Để truy cập một ô cụ thể trong mảng một chiều field, chúng ta cần tính chỉ số của ô đó.
	Chỉ số này được tính bằng công thức (cRow * N_COLS) + cCol, trong đó:
		cRow: là chỉ số hàng của ô (từ 0 đến N_ROWS - 1).
		cCol:  là chỉ số cột của ô (từ 0 đến N_COLS - 1).
 */
						doRepaint = true; // Đặt cờ cho phép vẽ lại bảng sau khi thay đổi trạng thái ô
						if (field[(cRow * N_COLS) + cCol] <= COVERED_MINE_CELL) { // Kiểm tra nếu ô hiện tại chưa được đánh dấu (không có cờ)
							if (minesLeft > 0) { // Kiểm tra nếu còn cờ để đặt (số mìn chưa đánh dấu lớn hơn 0)
								field[(cRow * N_COLS) + cCol] += MARK_FOR_CELL; // Đặt cờ (tăng giá trị ô bởi MARK_FOR_CELL:Ô được đánh dấu có cờ.)
								minesLeft--; // Giảm số lượng cờ còn lại đi 1
								String msg = Integer.toString(minesLeft); // Cập nhật thông báo số lượng mìn còn lại
								statusbar.setText(msg); // Hiển thị số lượng mìn còn lại trên thanh trạng thái
							} else {
								statusbar.setText("No marks left"); // Nếu không còn cờ để đặt, hiển thị thông báo
							}
						} else {
							field[(cRow * N_COLS) + cCol] -= MARK_FOR_CELL;
							minesLeft++;
							String msg = Integer.toString(minesLeft);
							statusbar.setText(msg);
						}
					}
				} else { // Nhấn chuột trái để mở ô
					if (field[(cRow * N_COLS) + cCol] > COVERED_MINE_CELL) {
						return;
					}

					if ((field[(cRow * N_COLS) + cCol] > MINE_CELL)
							&& (field[(cRow * N_COLS) + cCol] < MARKED_MINE_CELL)) {
						field[(cRow * N_COLS) + cCol] -= COVER_FOR_CELL;
						doRepaint = true;

						if (field[(cRow * N_COLS) + cCol] == MINE_CELL) {
							inGame = false;
						}

						if (field[(cRow * N_COLS) + cCol] == EMPTY_CELL) {
							find_empty_cells((cRow * N_COLS) + cCol);
						}
					}
				}

				if (doRepaint) {
					repaint();
				}
			}
		}
	}
}
