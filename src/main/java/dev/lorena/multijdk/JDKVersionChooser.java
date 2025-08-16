package dev.lorena.multijdk;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class JDKVersionChooser extends JDialog {
	
	private final Map<ButtonModel, JDK> jdkMap = new HashMap<>();
	private transient JDK choosenJDK = null;
	
	public JDKVersionChooser(List<JDK> jdks) {
		
		JPanel contentPanel = new JPanel();
		BorderLayout layout = new BorderLayout();
		
		contentPanel.setLayout(layout);
		
		setTitle("Choose JDK Version");
		setSize(600, 250);
		setLocationRelativeTo(null);
		setResizable(true);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setContentPane(contentPanel);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		Component jdkList = buildJDKsListComponent(jdks);
		
		contentPanel.add(buildTitlePanel(), BorderLayout.NORTH);
		contentPanel.add(jdkList, BorderLayout.CENTER);
		contentPanel.add(buildButtonsPanel(jdkList), BorderLayout.SOUTH);
		
		this.setVisible(true);
	}
	
	public JDK getChoosenJDK() {
		return choosenJDK;
	}
	
	private Component buildJDKsListComponent(List<JDK> jdks) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		ButtonGroup group = new ButtonGroup();

		jdks.stream()
			.sorted()
			.forEach(jdk -> {
				String text = String.format("Version: %s - Vendor: %s - Path: (%s)", jdk.getVersion(), jdk.getVendor(), jdk.getPath());
				log.debug("Adding JDK radio button: {}", text);
				JRadioButton radioButton = new JRadioButton(text);
				panel.add(radioButton);
				group.add(radioButton);
				jdkMap.put(radioButton.getModel(), jdk);
			});

		group.setSelected(group.getElements().nextElement().getModel(), true);
		panel.putClientProperty("buttonGroup", group);
		return new JScrollPane(panel);
	}
	
	private Component buildButtonsPanel(Component jdkListComponent) {
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton selectButton = new JButton("Select");
		JButton cancelButton = new JButton("Cancel");

		JPanel panel = (JPanel) ((JScrollPane) jdkListComponent).getViewport().getView();
		ButtonGroup group = (ButtonGroup) panel.getClientProperty("buttonGroup");

		selectButton.addActionListener(e -> {
			choosenJDK = jdkMap.get(group.getSelection());
			this.dispose();
			log.debug("Selected JDK: {}", choosenJDK);
		});
		cancelButton.addActionListener(e -> System.exit(0));

		buttonsPanel.add(selectButton);
		buttonsPanel.add(cancelButton);

		return buttonsPanel;
	}
	
	private Component buildTitlePanel() {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		String text = "Multple JDK's versions has been detected on your system. Please select the JDK version you want to use.";
		
		titlePanel.add(new JLabel(text));
		
		return titlePanel;
	}
	
}