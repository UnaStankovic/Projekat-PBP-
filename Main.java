package projekat;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

//Una Stankovic 127/2013	
//Katarina Rankovic 193/2013	
//Cedomir Dimic 163/2013

//MOST PROBABLY THE WORST CODE EVER WRITTEN! FUN TO SEE, HIDEOUS TO WRITE :) ENJOY!

public class Main {
	
	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
		    @Override
		    public void run()
		    {
		        Konekcija.close(con);
		    }
		});
		
	}
	
	public static void main(String[] args)
	{
		con = Konekcija.novaKonekcija("com.ibm.db2.jcc.DB2Driver", db_login.db_url, db_login.username, db_login.password);
		Main.ref = new Main();
		Main.ref.createGui();
	}
	
	public void createGui()
	{
		mainWindow = new JFrame("M01");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setSize(new Dimension(500, 700));
		mainWindow.setResizable(false);
		Container cont = mainWindow.getContentPane();
		SpringLayout layout = new SpringLayout();
		cont.setLayout(layout);
		mainPanel = new JPanel(new CardLayout());
		controlPanel = ref.createControlPanel();
		cont.add(controlPanel);
		
		M01P1 = ref.createPanelP1();
		M01P2 = ref.createPanelP2();
		M01P4 = ref.createPanelP4();
		
		mainPanel.add(M01P1, P1);
		mainPanel.add(M01P2, P2);
		mainPanel.add(M01P4, P4);
		((CardLayout) mainPanel.getLayout()).show(mainPanel, P1);
		currentPanel = P1;
		
		cont.add(mainPanel);
		
		
		layout.putConstraint(SpringLayout.NORTH, controlPanel, 0, SpringLayout.SOUTH, mainPanel);
		
		mainWindow.setVisible(true);
	}
	
	public JPanel createPanelP1()
	{
		SpringLayout layout = new SpringLayout();
		JPanel panel1 = new JPanel(layout);
		ref.setSize(panel1, 200, 300);
		
		//racunamo godinu koju cemo ubacivati u upit za smer
		String sql = "select distinct case when current_date >= concat('01.07.', year(current_date)) then year(current_date) else year(current_date) - 1 end from sysibm.systables";
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()) {
				godina = rs.getInt(1);
		}
			
		int month = Calendar.getInstance().get(Calendar.MONTH);
		int god = 0;
		if(month < 7)
			god = godina + 1;
		else
			god = godina;
		
		//pronalazimo smerove na master studijama i racunamo broj preostalih mesta
		ResultSet rs2 = null;
		
		PreparedStatement stmt2 = con.prepareStatement(upitSmer, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		stmt2.setInt(1, god);
		stmt2.setInt(2, god);
		rs2 = stmt2.executeQuery();
		
		//pravimo radio buttone ako ima mesta na smerovima
		//ako je prazna tabela znaci da trenutno niko nije upisan te godine i znaci da je broj
		//preostalih mesta ustvari jednak broju mesta navedenom u tabeli kvota
		if(!rs2.next()) { 
			String sql3 = "select id_smera, broj_budzetskih, broj_samofinansirajucih from kvota where godina=?";
			PreparedStatement stmt3 = con.prepareStatement(sql3);
			stmt3.setInt(1, godina);
			ResultSet rs3 = stmt3.executeQuery();
			while(rs3.next()) {
				String sql4 = "select trim(naziv) from smer where id_smera=?";
				PreparedStatement stmt4 = con.prepareStatement(sql4);
				stmt4.setInt(1, rs3.getInt(1));
				ResultSet rs4 = stmt4.executeQuery();
				while(rs4.next()) {
					//ima mesta na budzetu
					if(rs3.getInt(2) > 0) {
						String naziv = rs4.getString(1);
						dugmici[broj_smerova]=napraviDugme(naziv);
						broj_smerova++;
						mapStatus.put(rs3.getInt(1), "budzet");
						mapSmer.put(rs3.getInt(1), rs4.getString(1));
				    }
					//ima mesta na samofinansiranju
					else if(rs3.getInt(3) > 0) {
						String naziv = rs4.getString(1);	 
						dugmici[broj_smerova]=napraviDugme(naziv);
						broj_smerova++;
						mapStatus.put(rs3.getInt(1), "samofinansiranje");
					    mapSmer.put(rs3.getInt(1), rs4.getString(1));
					}

				}
				rs4.close();
				stmt4.close();
		    }
			rs3.close();
			stmt3.close();
		}
		
		//pronalazimo smerove ako ipak ima upisanih studenata te godine na neki smer
		else {
			while(rs2.next()) {
				//ima mesta na budzetu
				if(rs2.getInt(3) > 0) {
					if(!mapStatus.containsKey(rs2.getInt(1))) {
						String naziv = rs2.getString(2);
						dugmici[broj_smerova]=napraviDugme(naziv);
						broj_smerova++;
						mapStatus.put(rs2.getInt(1), "budzet");
						mapSmer.put(rs2.getInt(1), rs2.getString(2));
					}
			    }
				//ima mesta na samofinansiranju
				else if(rs2.getInt(4) > 0) {
					if(!mapStatus.containsKey(rs2.getInt(1))) {
						String naziv = rs2.getString(2);	 
						dugmici[broj_smerova]=napraviDugme(naziv);
						broj_smerova++;
						mapStatus.put(rs2.getInt(1), "samofinansiranje");
						mapSmer.put(rs2.getInt(1), rs2.getString(2));
					}
			   }
			}
			
			//proveravamo da li uopste ima mesta na nekom smeru
			if(mapStatus.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Svi smerovi su popunjeni!");
			}
			//grupisanje i rasporedjivanje dugmica
			for(int j=0; j<broj_smerova; j++) {
				grupa.add(dugmici[j]);
				panel1.add(dugmici[j]);
				if(j == 0){
					layout.putConstraint(SpringLayout.NORTH, dugmici[j],50, SpringLayout.NORTH, panel1);
					layout.putConstraint(SpringLayout.WEST, dugmici[j], 50, SpringLayout.WEST, panel1);
				}
				else {
					layout.putConstraint(SpringLayout.NORTH, dugmici[j],25, SpringLayout.NORTH, dugmici[j-1]);
					layout.putConstraint(SpringLayout.WEST, dugmici[j], 50, SpringLayout.WEST, panel1);
				}
			}		
		}
	
		rs2.close();
		}	
		catch (SQLException e) {
			Konekcija.error(e);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		izaberiBtn = new JButton("Izaberi");
		krajBtn = new JButton("Kraj");
		panel1.add(izaberiBtn);
		panel1.add(krajBtn);
		
		layout.putConstraint(SpringLayout.NORTH, izaberiBtn, 50, SpringLayout.SOUTH,dugmici[broj_smerova-1]);
		layout.putConstraint(SpringLayout.WEST, izaberiBtn, 150, SpringLayout.WEST,panel1);
		layout.putConstraint(SpringLayout.NORTH, krajBtn, 50, SpringLayout.SOUTH,dugmici[broj_smerova-1]);
		layout.putConstraint(SpringLayout.WEST, krajBtn, 100, SpringLayout.WEST, izaberiBtn);
		
		izaberiBtn.addActionListener  (  new ActionListener() {
	   
			    public void actionPerformed(ActionEvent e) {
				
				try
				{	for(j=0;j<broj_smerova;j++){
							if(dugmici[j].isSelected())
							{
							String smer=new String();
							smer=dugmici[j].getText();
							M01P3 = ref.createPanelP3(smer);
							
							mainPanel.add(M01P3, P3);
							}
						}
				
				//prelazak na sledeci panel
				
				((CardLayout) mainPanel.getLayout()).show(mainPanel, P2);
				nextBtn.setEnabled(true);
				currentPanel = P2;
				controlPanel.setVisible(true);
					
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}	
			}
		});
		
		krajBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					System.exit(0);
				}
				catch(Exception e1) {
					e1.printStackTrace();
				}
			} 
		});
		
		return panel1;
	}
	  
	public JRadioButton napraviDugme(String imeSmera) {
			JRadioButton dugme = new JRadioButton(imeSmera);
		    dugme.setActionCommand(imeSmera);
		    return dugme;
	}
	
	public String napraviIndeks(int ind,int god)
	{
		
		String indeks = new String(Integer.toString(godina));
	    
		//ako je ind = 1 nema jos nijednog studenta i zato ga ovako pravimo
		if(ind == 1) {
			indeks += "1001";
		}
		else {
			indeks += "1" + Integer.toString(ind).substring(5, 8);
		}
		return indeks;
	}
	
	public JPanel createPanelP2()
	{
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		ref.setSize(panel, 200, 300);

		int month = Calendar.getInstance().get(Calendar.MONTH);
		
		int god = 0;
		if(month < 7) {
			god = godina + 1;
		}
		else {
			god = godina;
		}
			
		String sql5 = "with pomocna(ind) as ( " + 
			  	   "select max(indeks) + 1 " +
			  	   "from dosije d join smer s " +
			  	   "on d.id_smera = s.id_smera " +
			  	   "join nivo_kvalifikacije nk on s.id_nivoa = nk.id_nivoa and nk.stepen = 'II' " +
			  	   "where year(datum_upisa) =?" +
			  	   ") " +
			  	   "select " +
			  	   "case when ind is null then 1 " +
			  	   "else ind end " +
			  	   "from pomocna";


		PreparedStatement stmt5;
		try {
			stmt5 = con.prepareStatement(sql5);
	
	
			stmt5.setInt(1, god);
			ResultSet rs5 = stmt5.executeQuery();
			int ind = 0;
			while(rs5.next()) {
				ind = rs5.getInt(1);
			}
			indeks=napraviIndeks(ind,god);
	
			rs5.close();
			stmt5.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	
		//pravimo formular za unos u dosije
		index = new JLabel("Indeks");
		indexfield = new JTextField(indeks, 25);
		indexfield.setEditable(false);
		name = new JLabel("Ime");
		namefield = new JTextField(25);
		surname = new JLabel("Prezime");
		surnamefield = new JTextField(25);
		sex = new JLabel("Pol");
		sexfield = new JTextField(25);
		jmbg = new JLabel("JMBG");
		jmbgfield = new JTextField(25);
		fname = new JLabel("Ime oca");
		fnamefield = new JTextField(25);
		mname = new JLabel("Ime majke");
		mnamefield = new JTextField(25);
		city = new JLabel("Mesto rodjenja");
		cityfield = new JTextField(25);
		bstate = new JLabel("Drzava rodjenja");
		bstatefield = new JTextField(25);
		street = new JLabel("Ulica stanovanja");
		streetfield = new JTextField(25);
		place = new JLabel("Mesto stanovanja");
		placefield = new JTextField(25);
		hnum = new JLabel("Kucni broj");
		hnumfield = new JTextField(25);
		post = new JLabel("Postanski broj");
		postfield = new JTextField(25);
		state = new JLabel("Drzava stanovanja");
		statefield = new JTextField(25);
		phone = new JLabel("Broj telefona");
		phonefield = new JTextField(25);
		mobile = new JLabel("Broj mobilnog");
		mobilefield = new JTextField(25);
		email = new JLabel("e-mail");
		emailfield = new JTextField(25);
		uri = new JLabel("uri");
		urifield = new JTextField(25);
		dateofsign = new JLabel("Datum upisa");
		dateofsignfield = new JTextField(25);
		Calendar cal = Calendar.getInstance();
		dateofsignfield.setText(cal.get(Calendar.DATE) + "." + "0" + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR));
		dateofsignfield.setEditable(false);
		dateofbirth = new JLabel("Datum rodjenja");
		dateofbirthfield = new JTextField(25);
	
		panel.add(index);
		panel.add(indexfield);
		panel.add(name);
		panel.add(namefield);
		panel.add(surname);
		panel.add(surnamefield);
		panel.add(sex);
		panel.add(sexfield);
		panel.add(jmbg);
		panel.add(jmbgfield);
		panel.add(fname);
		panel.add(fnamefield);
		panel.add(mname);
		panel.add(mnamefield);		    
		panel.add(city);
		panel.add(cityfield);
		panel.add(bstate);
		panel.add(bstatefield);
		panel.add(street);
		panel.add(streetfield);
		panel.add(place);
		panel.add(placefield);
		panel.add(hnum);
		panel.add(hnumfield);
		panel.add(post);
		panel.add(postfield);
		panel.add(state);
		panel.add(statefield);
		panel.add(phone);
		panel.add(phonefield);
		panel.add(mobile);
		panel.add(mobilefield);
		panel.add(email);
		panel.add(emailfield);
		panel.add(uri);
		panel.add(urifield);
		panel.add(dateofbirth);
		panel.add(dateofbirthfield);
		panel.add(dateofsign);		  
		panel.add(dateofsignfield);
	
		//index
		layout.putConstraint(SpringLayout.WEST, index, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, index, 35, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, indexfield, 35, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, indexfield, 52, SpringLayout.EAST, name);
		//name
		layout.putConstraint(SpringLayout.WEST, name, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, name, 60, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, namefield, 60, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, namefield, 52, SpringLayout.EAST, name);
		//surname
		layout.putConstraint(SpringLayout.WEST, surname, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, surname, 85, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, surnamefield, 85, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, surnamefield, 20, SpringLayout.EAST, surname);
		//sex
		layout.putConstraint(SpringLayout.WEST, sex, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, sex, 110, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, sexfield, 110, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, sexfield, 20, SpringLayout.EAST, surname);
		//jmbg
		layout.putConstraint(SpringLayout.WEST, jmbg, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, jmbg, 135, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, jmbgfield, 135, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, jmbgfield, 42, SpringLayout.EAST, jmbg);
		//fathers name
		layout.putConstraint(SpringLayout.WEST, fname, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, fname, 160, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, fnamefield, 160, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, fnamefield, 25, SpringLayout.EAST, fname);
		//mothers name
		layout.putConstraint(SpringLayout.WEST, mname, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, mname, 185, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, mnamefield, 185, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, mnamefield, 42, SpringLayout.EAST, jmbg);
		//city
		layout.putConstraint(SpringLayout.WEST, city, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, city, 220, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, cityfield, 220, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, cityfield, 42, SpringLayout.EAST, city);
		//bstate 
		layout.putConstraint(SpringLayout.WEST, bstate, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, bstate, 245, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, bstatefield, 245, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, bstatefield, 38, SpringLayout.EAST, bstate);
		//street
		layout.putConstraint(SpringLayout.WEST, street, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, street, 270, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, streetfield, 270, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, streetfield, 32, SpringLayout.EAST, street);
		//place
		layout.putConstraint(SpringLayout.WEST, place, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, place, 295, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, placefield, 295, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, placefield, 24, SpringLayout.EAST, place);
		//hnum
		layout.putConstraint(SpringLayout.WEST, hnum, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, hnum, 320, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, hnumfield, 320, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, hnumfield, 80, SpringLayout.EAST, hnum);
		//post
		layout.putConstraint(SpringLayout.WEST, post, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, post, 345, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, postfield, 345, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, postfield, 48, SpringLayout.EAST, post);
		//state
		layout.putConstraint(SpringLayout.WEST, state, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, state, 370, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH,statefield, 370, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, statefield, 19, SpringLayout.EAST, state);
		//phone
		layout.putConstraint(SpringLayout.WEST, phone, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, phone, 405, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, phonefield, 405, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, phonefield, 58, SpringLayout.EAST, phone);
		//mobile
		layout.putConstraint(SpringLayout.WEST, mobile, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, mobile, 430, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, mobilefield, 430, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, mobilefield, 52, SpringLayout.EAST, mobile); 
		//email
		layout.putConstraint(SpringLayout.WEST, email, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, email, 455, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, emailfield, 455, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, emailfield, 106, SpringLayout.EAST, email); 
		//uri
		layout.putConstraint(SpringLayout.WEST, uri, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, uri, 480, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, urifield, 480, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, urifield, 130, SpringLayout.EAST, uri);
		//date of birth
		layout.putConstraint(SpringLayout.WEST, dateofbirth, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, dateofbirth, 505, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, dateofbirthfield, 505, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, dateofbirthfield, 40, SpringLayout.EAST, dateofbirth); 
		//date of sign
		layout.putConstraint(SpringLayout.WEST, dateofsign, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, dateofsign, 530, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, dateofsignfield, 530, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, dateofsignfield, 60, SpringLayout.EAST, dateofsign); 
	
		return panel;
	}
		
	public JPanel createPanelP3(String smer) {
		
		JPanel panel = new JPanel(new GridLayout(0,1));
		ref.setSize(panel, 400, 600);
		String smerID = "select id_smera from smer s join nivo_kvalifikacije n on s.id_nivoa=n.id_nivoa where n.stepen='II'and s.naziv=?";		
		panel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		//pravimo panel za odabir predmeta
		try{
			PreparedStatement stmtSmer = con.prepareStatement(smerID);
			stmtSmer.setString(1, smer);
			PreparedStatement stmt6 = con.prepareStatement(upitObavezni);
			ResultSet rsSmer = stmtSmer.executeQuery();
			if(rsSmer.next()) {
				id_smera=rsSmer.getInt(1);
			}
			status = mapStatus.get(id_smera);
			stmt6.setInt(1, id_smera);
			ResultSet rs6 = stmt6.executeQuery();
		   	while(rs6.next()) {
		   		JCheckBox chk = new JCheckBox(rs6.getString(1), true);
		   		chk.setEnabled(false);
		   		panel.add(chk);
		   		boxovi[broj_predmeta] = chk;
		   		broj_predmeta++;
		   		predmetiBodovi.put(rs6.getString(1), rs6.getInt(2));
		   	}
			
		   	obavezniBrojac = broj_predmeta;
		   	
		   	rs6.close();
		   	stmt6.close();
		   	
		    PreparedStatement stmt7 = con.prepareStatement(upitIzborni);
			stmt7.setInt(1, id_smera);
			ResultSet rs7 = stmt7.executeQuery();
		   	while(rs7.next()) {
		   		JCheckBox chk = new JCheckBox(rs7.getString(1));
		   		chk.setEnabled(true);
		   		panel.add(chk);
		   		boxovi[broj_predmeta] = chk;
		   		broj_predmeta++;
		   		predmetiBodovi.put(rs7.getString(1), rs7.getInt(2));
		   	}

		   	rs7.close();
		   	stmt7.close();
		}
		catch (SQLException e) {
			Konekcija.error(e);
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		
		return panel;		
	}
	
	public JPanel createPanelP4()
	{
		//pravimo poslednji panel gde ce bit ispisano da je student upisan
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		JLabel uspeh = new JLabel("Uspesno upisan student");
		panel.add(uspeh);
		JLabel podaci = new JLabel("Ime i prezime: " + namefield.getText() + surnamefield.getText() +
				" Indeks: " + indexfield.getText() + " Skolska godina: " + godina);
		panel.add(podaci);
		JButton kraj = new JButton("Kraj");
		panel.add(kraj);
		layout.putConstraint(SpringLayout.NORTH, uspeh, 20, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, podaci, 40, SpringLayout.WEST, uspeh);
		layout.putConstraint(SpringLayout.NORTH, kraj, 60, SpringLayout.WEST, podaci);
		kraj.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					System.exit(0);
			}
		});
		
		ref.setSize(panel, 500, 500);
		return panel;
	}
	
	public JPanel createControlPanel()
	{
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel(layout);
		ref.setSize(panel, 500, 200);
		panel.setVisible(false);
		nextBtn = new JButton("sledeća");
		nextBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(currentPanel == P2)
				{	
					//unos u dosije
					try{
						String sql = "insert into dosije " +
								     "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					    
						//indikator za proveru da li je korektan unos
						boolean unos = true;
						PreparedStatement stmt = con.prepareStatement(sql);
						String index = indexfield.getText();
						int indeks = Integer.parseInt(index);
						
						//provere ispravnosti unosa podataka
						if(!isCorrectSize(namefield.getText(), 1, 25) || Character.isLowerCase(namefield.getText().charAt(0)) || isNull(namefield.getText())) {
							unos = false;
						}
						if(!isCorrectSize(surnamefield.getText(), 1, 25) || Character.isLowerCase(surnamefield.getText().charAt(0)) || isNull(surnamefield.getText())) {
							unos = false;
						}
						if((!sexfield.getText().equals("musko") && !sexfield.getText().equals("zensko")) || isNull(sexfield.getText())) {
							unos = false;
						}
						if(!isCorrectSize(jmbgfield.getText(), 13, 13) || !allDigits(jmbgfield.getText()) || isNull(jmbgfield.getText())) {
							unos = false;
						}
						if(!isValidDate(dateofbirthfield.getText())) {
							unos = false;
						}
						if(!isCorrectSize(cityfield.getText(), 1, 100) || Character.isLowerCase(cityfield.getText().charAt(0))) {
							unos = false;
						}
						if(!isCorrectSize(bstatefield.getText(), 1, 100) || Character.isLowerCase(bstatefield.getText().charAt(0))) {
							unos = false;
						}
						if(!isCorrectSize(streetfield.getText(), 1, 100) || Character.isLowerCase(streetfield.getText().charAt(0))) {
							unos = false;
						}
						if(!isCorrectSize(mnamefield.getText(), 1, 50) || Character.isLowerCase(mnamefield.getText().charAt(0))) {
							unos = false;
						}
						if(!isCorrectSize(fnamefield.getText(), 1, 50) || Character.isLowerCase(fnamefield.getText().charAt(0))) {
							unos = false;
						}
						if(!isCorrectSize(placefield.getText(), 1, 100) || Character.isLowerCase(placefield.getText().charAt(0))) {
							unos = false;
						}
						if(!isCorrectSize(statefield.getText(), 1, 100) || Character.isLowerCase(statefield.getText().charAt(0))) {
							unos = false;
						}
						if(!isCorrectSize(hnumfield.getText(), 1, 20) || !allDigits(hnumfield.getText())) {
							unos = false;
						}
						if(!isCorrectSize(postfield.getText(), 1, 20) || !allDigits(postfield.getText())) {
							unos = false;
						}
						if(!isCorrectSize(mobilefield.getText(), 1, 25) || !isValidNumber(mobilefield.getText())) {
							unos = false;
						}
						if(!isCorrectSize(phonefield.getText(), 1, 25) || !isValidNumber(phonefield.getText())) {
							unos = false;
						}
						if(!isCorrectSize(emailfield.getText(), 1, 50)) {
							unos = false;
						}
						if(!isCorrectSize(urifield.getText(), 1, 100)) {
							unos = false;
						}
						
						
						if(unos == false) {
							JOptionPane.showMessageDialog(null, "Neispravan unos");
						}
						else {
							String name = namefield.getText();
							String surname = surnamefield.getText();
							String sex = sexfield.getText();
							String jmbg = jmbgfield.getText();	
							String date_of_birth = dateofbirthfield.getText();
							String[] tokens = date_of_birth.split("\\.");
							java.sql.Date birth = java.sql.Date.valueOf(tokens[2] + "-" + tokens[1] + "-" + tokens[0]); 
							String place_of_birth = cityfield.getText();
							String state_of_birth = bstatefield.getText();
							String fname = fnamefield.getText();
							String mname = mnamefield.getText();
							String street = streetfield.getText();
							String hnum = hnumfield.getText();
							String place = placefield.getText();
							String post = postfield.getText();
							String state = statefield.getText();
							String phone = phonefield.getText();
							String mobile = mobilefield.getText();
							String email = emailfield.getText();
							String uri = urifield.getText();
							String date_of_sign = dateofsignfield.getText();
							tokens = date_of_sign.split("\\.");
							java.sql.Date sign = java.sql.Date.valueOf(tokens[2] + "-" + tokens[1] + "-" + tokens[0]);   				
    				 		
							stmt.setInt(1, indeks);
							stmt.setInt(2, id_smera);
							stmt.setString(3, name);
							stmt.setString(4, surname);
				   		    if(sex.equals("musko"))
				   		    	stmt.setString(5, "m");
				   		    else
				   		    	stmt.setString(5, "z");
				   		    stmt.setString(6, jmbg);
				   		    stmt.setDate(7,birth);
				   		    stmt.setString(8, place_of_birth);
				   		    stmt.setString(9, state_of_birth);
				   		    stmt.setString(10, fname);
				   		    stmt.setString(11, mname);
				   		    stmt.setString(12, street);
				   		    stmt.setString(13, hnum);
				   		    stmt.setString(14, place);
				   		    stmt.setString(15, post);
				   		    stmt.setString(16, state);
				   		    stmt.setString(17, phone);
				   		    stmt.setString(18, mobile);
				   		    stmt.setString(19, email);
				   		    stmt.setString(20, uri);
				   		    stmt.setDate(21, sign);
				   		    int inss=stmt.executeUpdate();
				   		    if(inss==1) {
				   		    	Konekcija.commit(con);
				   		    	JOptionPane.showMessageDialog(null, "Podaci studenta su uspešno uneti.");
				   		    }
				   		    else {
				   		    	Konekcija.rollback(con);
				   		    	JOptionPane.showMessageDialog(null, "Unošenje neuspešno.");
				   		    }
				   		    stmt.close();
				   		    con.setAutoCommit(true);
						}
				}	
				catch(SQLException e1) {
    				Konekcija.error(e1);
    				Konekcija.rollback(con);
    			}
    			catch(Exception e1){
    				e1.printStackTrace();
    				Konekcija.rollback(con);
    			}
    				
    				//prelazak na sledeci panel
					
					((CardLayout) mainPanel.getLayout()).show(mainPanel, P3);
					currentPanel = P3;
				}
				
				else if(currentPanel == P3)
				{
						
			    	String index = indexfield.getText();
    				int indeks = Integer.parseInt(index);
    				String jmbg = jmbgfield.getText();
    				
    				//indikator koji cuva informacija da li je sve ispravno uneseno,
    				//da li je broj izabranih bodova u intervalu [55, 65],
    				//da li su polozeni uslovni predmeti
    				
    				boolean ind = true;
    				
    				for(j=obavezniBrojac+1; j<broj_predmeta; j++) {
    					if(boxovi[j].isSelected()) {
    						try {
				    			PreparedStatement stmt8 = con.prepareStatement(upitUslovni);
				    			String sqlPom = "select id_predmeta from predmet where naziv=?";
				    			PreparedStatement stmt9 = con.prepareStatement(sqlPom);
				    			stmt9.setString(1, boxovi[j].getText());
				    			ResultSet rs9 = stmt9.executeQuery();
				    			int id_predmeta = 0;
				    			while(rs9.next()) {
				    				id_predmeta = rs9.getInt(1);
				    			}
				    			stmt8.setInt(1, id_predmeta);
				    			stmt8.setString(2, jmbg);
				    			ResultSet rs8 = stmt8.executeQuery();
				    			if(rs8.next()) {
				    				int ans = JOptionPane.showOptionDialog(null, "Za predmet " + boxovi[j].getText().trim() + " nisu polozeni uslovni predmeti! Da li zelite da ga ipak dodate?", "Nepolozeni uslovni predmeti", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Potvrdi",  "Odustani"}, "default");
				    				if(ans == JOptionPane.CANCEL_OPTION) {
				    					ind = false;
				    				}
				    				broj_poena = 0;
				    			}
				    		}
				    		catch(SQLException e1) {
				    			Konekcija.error(e1);
				    		}
				    		catch(Exception e1) {
				    			e1.printStackTrace();
				    		}
    					}
    				}
    				
    				if(ind == true) {
    					for(j=0; j<broj_predmeta; j++) {	
    						if (boxovi[j].isSelected())	{
    							int poeni=predmetiBodovi.get(boxovi[j].getText());
    							broj_poena+=poeni;
    						}				
    					}
    				}
    				
    				if(broj_poena > 65) {
						JOptionPane.showMessageDialog(null, "Izabrali ste vise od 65 ESPB bodova! Broj bodova mora biti u intervalu [55, 65]!");
						ind = false;
						broj_poena = 0;
    				}
    				else if(broj_poena < 55) {
    					JOptionPane.showMessageDialog(null, "Izabrali ste manje od 55 ESPB bodova! Broj bodova mora biti u intervalu [55, 65]!");
    					ind = false;
    					broj_poena = 0;
    				}
    			    else {
    			    	//popunjavamo tabele upisan_kurs, upis_godine i status
    			    	//ako je odgovarajuci broj izabranih poena
    					int ins1 = 0;
    					int pred = 0;
    					try {
    					
    						String sql2 = "insert into upis_godine " +
    								  	  "values(?, ?, ?, ?, ?, ?)";
    						PreparedStatement stmt2=con.prepareStatement(sql2);
  
    						stmt2.setInt(1, indeks);
    						stmt2.setInt(2, godina);
    						stmt2.setDate(3, sign);
    						stmt2.setInt(4,broj_poena);
    						stmt2.setNull(5, java.sql.Types.DATE);
    						stmt2.setNull(6, java.sql.Types.SMALLINT);
    						int ins2;
    						ins2 = stmt2.executeUpdate();
				
    						String sql3 = "insert into status " +
    									  "values(?, ?, ?)";	
    						PreparedStatement stmt3=con.prepareStatement(sql3);
    						stmt3.setInt(1,indeks);
    						stmt3.setDate(2,sign);
    						stmt3.setString(3,status);
    						int ins3;
    						ins3 = stmt3.executeUpdate();
					
    						int tmp = 0;
    						for(j=0;j<broj_predmeta;j++) {
    							if (boxovi[j].isSelected()) {		
    								tmp++;
    								String sql1 = "insert into upisan_kurs " +
											  	  "values(?, ?, ?, ?)";
    								PreparedStatement stmt1= con.prepareStatement(sql1);
    								String predmetID = "select id_predmeta from predmet  where naziv=?";		
    								PreparedStatement stmtPredmet = con.prepareStatement(predmetID);
    								stmtPredmet.setString(1,boxovi[j].getText());
    								ResultSet rs = stmtPredmet.executeQuery();
								
    								if(rs.next()) {
    									pred=rs.getInt(1);
    								}
								
    								stmt1.setInt(1,indeks);
    								stmt1.setInt(2,pred);
    								stmt1.setInt(3,godina);
    								stmt1.setInt(4,1);
    								stmtPredmet.close();
				
    								ins1 += stmt1.executeUpdate();
    								stmt1.close();
    								rs.close();
    						}
						
    					}				
    						
    						if((ins3 + ins2) == 2 && ins1 == tmp) {
    							Konekcija.commit(con);
    						}
    						else {
    							Konekcija.rollback(con);
    							JOptionPane.showMessageDialog(null, "Unošenje neuspešno.");
    						}
    						
    						stmt2.close();
    						stmt3.close();
    						con.setAutoCommit(true);
    					}
    					catch(SQLException e1) {
    						Konekcija.error(e1);
    					}
    					catch (Exception e1) {
    						e1.printStackTrace();
    					}
    			}
    									
    			//prelazak na sledeci panel
    			if(ind == true){
		    		((CardLayout) mainPanel.getLayout()).show(mainPanel, P4);
		    		nextBtn.setEnabled(false);
		    		currentPanel = P4;
    			}
						
			}
				else if(currentPanel == P1) {
					((CardLayout) mainPanel.getLayout()).show(mainPanel, P2);
					nextBtn.setEnabled(true);
					currentPanel = P2;
				}
				
			}
		});
		
		panel.add(nextBtn);
		layout.putConstraint(SpringLayout.NORTH, nextBtn, 10, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, nextBtn, 170, SpringLayout.WEST, panel);
		return panel;
	}
	
	
	
	public void setSize(Component a, int width, int heigth)
	{
		a.setPreferredSize(new Dimension(width, heigth));
		a.setSize(width, heigth);
		a.setMaximumSize(new Dimension(width, heigth));
	}

	public static Connection con = null;
	
	//globalne promenljive vezane za panele
	
	public final static String P1 = "P1";
	public final static String P2 = "P2";
	public final static String P3 = "P3";
	public final static String P4 = "P4";
	
	public static Main ref;
	private JFrame mainWindow;
	private JPanel mainPanel;
	public static String currentPanel;
	
	public static JPanel controlPanel;
	public static JButton nextBtn;
	
	public static JPanel M01P1;
	public static JPanel M01P2;
	public static JPanel M01P3;	
	public static JPanel M01P4;


	public static JLabel index;
	public static JTextField indexfield;
	public static JLabel name;
	public static JTextField namefield;
	public static JLabel surname;
	public static JTextField surnamefield;
	public static JLabel sex;
	public static JTextField sexfield;
	public static JLabel jmbg;
	public static JTextField jmbgfield;
	public static JLabel fname;
	public static JTextField fnamefield;
	public static JLabel mname;
	public static JTextField mnamefield;
	public static JLabel city;
	public static JTextField cityfield;
	public static JLabel bstate;
	public static JTextField bstatefield;
	public static JLabel street;
	public static JTextField streetfield;
	public static JLabel place;
	public static JTextField placefield;
	public static JLabel hnum;
	public static JTextField hnumfield;
	public static JLabel post;
	public static JTextField postfield;
	public static JLabel state;
	public static JTextField statefield;
	public static JLabel phone;
	public static JTextField phonefield;
	public static JLabel mobile;
	public static JTextField mobilefield;
	public static JLabel email;
	public static JTextField emailfield;
	public static JLabel uri;
	public static JTextField urifield;
	public static JLabel dateofsign;
	public static JTextField dateofsignfield;
	public static JLabel dateofbirth;
	public static JTextField dateofbirthfield;
	
	public static JButton izaberiBtn;
	public static JButton krajBtn;
	ButtonGroup grupa = new ButtonGroup();
	JRadioButton[] dugmici=new JRadioButton[100];
	JCheckBox[] boxovi = new JCheckBox[30];
	
	//globalne promenljive za upite
	
	String upitSmer = "with budzetski (br, naziv, id_smera, godina) as ( " +
		      		  "select count(d.indeks), sm.naziv, sm.id_smera, year(datum_upisa) " +
		      		  "from smer sm left outer join dosije d on sm.id_smera = d.id_smera " +	
		      		  "left outer join status s on s.indeks = d.indeks and s.status = 'budzet' " + 
		      		  "join nivo_kvalifikacije nk on nk.id_nivoa = sm.id_nivoa and nk.stepen = 'II' " +
		      		  "where year(datum_upisa)=? " +
		      		  "group by sm.id_smera, sm.naziv, year(datum_upisa) " +
		      		  "), " +
		      		  "samofinansirajuci (br, naziv, id_smera, godina) as ( " +
		      		  "select count(d.indeks), sm.naziv, sm.id_smera, year(datum_upisa) " +
		      		  "from smer sm left outer join dosije d on sm.id_smera = d.id_smera " +
		      		  "left outer join status s on s.indeks = d.indeks and s.status = 'samofinansiranje' " +
		      		  "join nivo_kvalifikacije nk on nk.id_nivoa = sm.id_nivoa and nk.stepen = 'II' " +
		      		  "where year(datum_upisa)=? " +
		      		  "group by sm.id_smera, sm.naziv, year(datum_upisa) " +
		      		  "), " +
		      		  "smerovi_kvote (id_smera, naziv, kvota_budzet, preostalo_budzet, kvota_samofinansiranje, preostalo_samofinansiranje) as ( " +
		      		  "select distinct sm.id_smera, sm.naziv, k.broj_budzetskih, k.broj_budzetskih - b.br, k.broj_samofinansirajucih, k.broj_samofinansirajucih - s.br " + 
		      		  "from kvota k join budzetski b on k.id_smera = b.id_smera and k.godina = b.godina " +
		      		  "join samofinansirajuci s on k.id_smera = s.id_smera and k.godina = s.godina " +
		      		  "join smer sm on k.id_smera = sm.id_smera " +
		      		  "), " +
		      		  "smerovi as ( " +
		      		  "select distinct sk.id_smera, s.naziv, kvota_budzet, preostalo_budzet, kvota_samofinansiranje, preostalo_samofinansiranje " +
		      		  "from smer s left outer join smerovi_kvote sk on s.id_smera = sk.id_smera " +
		      		  "join nivo_kvalifikacije nk on s.id_nivoa = nk.id_nivoa and nk.stepen = 'II' " +
		      		  ") " +
		      		  "select distinct sm.id_smera, trim(sm.naziv), case when sm.preostalo_budzet is null then sk.kvota_budzet else sm.preostalo_budzet end, " +
		      		  "case when sm.preostalo_samofinansiranje is null then sk.kvota_samofinansiranje else sm.preostalo_samofinansiranje end " +
		      		  "from smerovi sm, smerovi_kvote sk";
	
	String upitObavezni = "select p.naziv, p.bodovi " +
			  			  "from predmet p join obavezan_predmet op on p.id_predmeta = op.id_predmeta " +	
			  			  "join smer s on op.id_smera = s.id_smera " +
			  			  "join nivo_kvalifikacije nk on s.id_nivoa = nk.id_nivoa and nk.stepen = 'II' " + 
			  			  "where s.id_smera=?";
	
	String upitIzborni = "select i.naziv, i.bodovi " +
			  			 "from izborni_predmet_master i " +	
			  			 "where i.id_smera=?";
	
	String upitUslovni = "with uslovni(id_predmeta, id_uslovnog) as " + 
     		 			 "( " +	   
     		 			 "select up.id_predmeta, up.id_uslovnog " +
     		 			 "from uslovni_predmet up join predmet p on up.id_predmeta = p.id_predmeta " +
     		 			 "where up.id_predmeta =? " +

     					 "union all " +

     					 "select up.id_predmeta, up.id_uslovnog " +
     					 "from uslovni_predmet up, uslovni u " +
     					 "where u.id_uslovnog = up.id_predmeta " +
     					 ") " +
     					 "select id_uslovnog, trim(naziv) " +
     					 "from uslovni u join predmet p on u.id_predmeta = p.id_predmeta " +
     					 "where id_uslovnog not in " +
     					 "(select id_predmeta from ispit i join dosije d on i.indeks = d.indeks " + 
     					 "where status_prijave = 'o' and ocena > 5 and jmbg=?)";
	
	//dodatne, pomocne promenljive i strukture za smestanje podataka		 
	//Hes mapa u kojoj se cuva status studenta u zavisnosti od smera koji je izabrao
	//i hes mapa u kojoj se cuvaju nazivi smerova
	HashMap<Integer, String> mapStatus = new HashMap<Integer, String>();
	HashMap<Integer, String> mapSmer = new HashMap<Integer, String>();
	int broj_smerova=0;//broji dugmice
	int godina = 0; //izdvaja godinu 
	int obavezniBrojac; //brojimo obavezne predmete, potrebno nam je zbog dodavanja uslovnih kasnije
	String indeks = new String(); //cuvamo indeks
	//hes mape gde se cuvaju predmeti i njihovi nazivi i predmeti i njihovi bodovi
	HashMap<Integer, String> predmetiMap = new HashMap<Integer, String>();
	HashMap<String, Integer> predmetiBodovi = new HashMap<String, Integer>();
	//set u kom se cuvaju nazivi nepolozenih uslovnih predmeta ako ih ima
	Set<String> nepolozeniPredmeti = new LinkedHashSet<String>();
	public String status; //cuvamo status studenta
	public int id_smera; //cuvamo id_smera na koji se student upisuje
	int broj_predmeta=0; //broji checkboxove
	int broj_poena; //cuvamo broj poena odabranih predmeta
	int j;
	
	//parsiranje datuma
	Calendar cal = Calendar.getInstance();
	String date=(cal.get(Calendar.DATE) + "." + "0" + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR));
	String[] tokens=date.split("\\.");;
	java.sql.Date sign = java.sql.Date.valueOf(tokens[2] + "-" + tokens[1] + "-" + tokens[0]);

	//pomocne funkcije za proveru ispravnosti unetih podataka za dosije
	public static boolean isValidNumber(String data) {
		return data.matches("[0-9]+[/-][0-9]+-[0-9]+");
	}
	
	public static boolean isCorrectSize(String data, int min, int max) {
		if(data.length() < min || data.length() > max)
			return false;
		return true;
	}
	
	public static boolean allDigits(String data) {
		for(int i=0; i<data.length(); i++)
			if(!Character.isDigit(data.charAt(i)))
				return false;
		return true;
	}
	
	public static boolean isValidDate(String data) {
		try {
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			df.setLenient(false);
			df.parse(data);
			return true;
		}
		catch(ParseException e) {
			return false;
		}
	}
	
	public static boolean isNull(String data) {
		if(data == null || data.trim().equals(""))
			return true;
		return false;
	}
	
}