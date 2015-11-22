package sami.hust.edu.vn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa;
import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa.DiemXet;
import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa.DiemXet.NVXet;
import sami.hust.edu.vn.ProtoModels.ListKey;
import sami.hust.edu.vn.ProtoModels.MyKey;

import com.google.protobuf.InvalidProtocolBufferException;

public class WriteExellDSTrungTuyen {

	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private String inputFile;

	public static void main(String[] args) {

		new WriteExellDSTrungTuyen().run();
		
	}
	public void run(){
		setOutputFile("d:/DATN/Result/DSTrungTuyen.xls");
		try {
			write();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteExell(WritableSheet sheet, NVXet nv, String mang, int row) {
		try {
			addLabel(sheet, 0, row, nv.getSbd());
			addNumberDouble(sheet, 1, row, nv.getDiem());
			addLabel(sheet, 2, row, mang);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	public void setOutputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void write() throws IOException, WriteException {
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(new Locale("en", "EN"));

		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Report", 0);
		WritableSheet excelSheet = workbook.getSheet(0);
		createLabel(excelSheet);
		MyKey key = MyKey.newBuilder().setKey("ALL_KHOA").build();
		try {
			ListKey mlist = ListKey.parseFrom(RedisClient.shareInstance().get(
					key.toByteArray()));
			List<String> allKhoa = mlist.getKeyList();
			int i = 1;
			for (String makhoa : allKhoa) {
				key = MyKey.newBuilder().setKey(makhoa).build();
				ChiTieuKhoa result = ChiTieuKhoa.parseFrom(RedisClient
						.shareInstance().get(key.toByteArray()));
				ArrayList<DiemXet> list = new ArrayList<DiemXet>();
				list.addAll(result.getDSDiemXetList());
				for (DiemXet diemXet : list)
					for (NVXet nv : diemXet.getDSNVXetList()) {
						WriteExell(excelSheet, nv, makhoa, i);
						i++;
					}

			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		workbook.write();
		workbook.close();
	}

	private void createLabel(WritableSheet sheet) throws WriteException {
		// Lets create a times font
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(false);
		// create create a bold font with unterlines
		WritableFont times10ptBoldUnderline = new WritableFont(
				WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		// Lets automatically wrap the cells
		timesBoldUnderline.setWrap(false);
		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);

		// Write a few headers
		// Write a few headers
		addCaption(sheet, 0, 0, "SBD");
		addCaption(sheet, 1, 0, "DiemXT");
		addCaption(sheet, 2, 0, "MaNganh");

	}

	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);

	}

	private void addNumberDouble(WritableSheet sheet, int column, int row,
			Double db) throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, db, times);
		sheet.addCell(number);
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
	}

}
