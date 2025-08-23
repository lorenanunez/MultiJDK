package dev.lorena.multijdk;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class JDKVersionChooser extends JDialog {
	
	private final Map<ButtonModel, JDK> jdkMap = new HashMap<>();
	private transient Settings settings = null;
	private transient JDK choosenJDK = null;
	
	public JDKVersionChooser(List<JDK> jdks) {
		
		settings = SettingsManager.getSettings();
		
		JPanel contentPanel = new JPanel();
		BorderLayout layout = new BorderLayout();
		
		contentPanel.setLayout(layout);
		
		setTitle("Choose JDK Version");
		setSize(600, 250);
		setMinimumSize(new Dimension(600, 250));
		setLocationRelativeTo(null);
		setResizable(true);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setContentPane(contentPanel);
		setIconImage(getAppIcon());
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		JCheckBox rememberChoiceCheckBox = buildCheckBoxPanel();
		Component jdkList = buildJDKsListComponent(jdks);
		contentPanel.add(buildTitlePanel(), BorderLayout.NORTH);
		contentPanel.add(jdkList, BorderLayout.CENTER);
		contentPanel.add(buildBottomPanel(rememberChoiceCheckBox, buildButtonsPanel(jdkList, rememberChoiceCheckBox)), BorderLayout.SOUTH);
		
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
	
	private Component buildButtonsPanel(Component jdkListComponent, JCheckBox rememberChoiceCheckBox) {
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton selectButton = new JButton("Select");
		JButton cancelButton = new JButton("Cancel");

		JPanel panel = (JPanel) ((JScrollPane) jdkListComponent).getViewport().getView();
		ButtonGroup group = (ButtonGroup) panel.getClientProperty("buttonGroup");

		selectButton.addActionListener(e -> {
			choosenJDK = jdkMap.get(group.getSelection());
			this.dispose();
			log.debug("Selected JDK: {}", choosenJDK);
			
			if (rememberChoiceCheckBox.isSelected()) {
				
				if (settings.getPreferredJDKPerFile() == null) {
					settings.setPreferredJDKPerFile(new HashMap<>());
				}
				
				Optional<Arguments> args = ArgumentsHandler.getParsedArguments();
				
				if (args.isPresent()) {
					settings.getPreferredJDKPerFile().put(args.get().getJarPath(), choosenJDK.getPath());
				} else {
					JOptionPane.showMessageDialog(this, "Error getting the JAR file path from arguments. Cannot remember the choice.", "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				
				SettingsManager.saveSettings(settings);
			}
		});
		cancelButton.addActionListener(e -> System.exit(0));

		buttonsPanel.add(selectButton);
		buttonsPanel.add(cancelButton);

		return buttonsPanel;
	}
	
	
	private JCheckBox buildCheckBoxPanel() {
		JCheckBox rememberChoiceCheckBox = new JCheckBox("Remember this JDK for this JAR");
		rememberChoiceCheckBox.setSelected(true);
		return rememberChoiceCheckBox;
	}
	private Component buildBottomPanel(Component rememberComboBox, Component buttonsComponent) {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		
		bottomPanel.add(rememberComboBox, BorderLayout.EAST);
		bottomPanel.add(buttonsComponent, BorderLayout.WEST);
		
		return bottomPanel;
	}
	
	private Component buildTitlePanel() {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		String text = "Multple JDK's versions has been detected on your system. Please select the JDK version you want to use.";
		
		titlePanel.add(new JLabel(text));
		
		return titlePanel;
	}
	
	private Image getAppIcon() {
		try {
			InputStream is = JDKVersionChooser.class.getResourceAsStream("/thin-mug-hot.png");
			Image image = ImageIO.read(is);
			
			return image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			log.error("Error loading app icon", e);
			return null;
		}
	}
	
}