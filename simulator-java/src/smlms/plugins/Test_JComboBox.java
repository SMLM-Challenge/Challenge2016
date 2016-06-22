package smlms.plugins;

import java.awt.Font;

import javax.swing.JComboBox;
import javax.swing.JFrame;

public class Test_JComboBox {

	public static void main(String args[]) {
		new Test_JComboBox();
	}
	
	public Test_JComboBox() {
		JFrame frame = new JFrame("Test");
		JComboBox cmb = new JComboBox(new String[] {"x y z", "t u ? ? z"});
		cmb.setEditable(true);
		Font font = cmb.getFont();
		frame.getContentPane().add(cmb);
		cmb.setFont(new java.awt.Font(font.getFamily(), Font.BOLD, font.getSize()+10 ));
		frame.pack();
		frame.setVisible(true);
	}

}
