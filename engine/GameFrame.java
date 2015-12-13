package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Klasa namijenjena jednostavnim grafi?kim eksperimentima i demonstracijama. Koristi se
 * naslje?ivanjem i popunjavanjem apstraktnih metoda poput update() i render(), uz
 * metode koje obra?uju input doga?aje, ako je potrebna interakcija.
 * @author Aleksandar Stan?i?
 *
 */
public abstract class GameFrame extends JPanel implements MouseListener, 
	MouseWheelListener, MouseMotionListener, KeyListener
{
	private static final long serialVersionUID = 6058915663486070170L;

	public static enum GFMouseButton
	{
		None,
		Left,
		Middle,
		Right,
		WheelUp,
		WheelDown,
		WheelLeft,
		WheelRight,
		Special1,
		Special2
	}

	private int screenX = 640;
	private int screenY = 480;
	
	private static JFrame myFrame = null;
	private String title = "RAF GameFrame";
	
	private Color backColor = Color.black;
	private boolean clearBackBuffer = true;
	
	private int mouseX = 0;
	private int mouseY = 0;
	
	private int updateRate = 30;
	private boolean updatesRunning = true;
	private boolean renderRunning = true;
	
	private boolean useHQ = false;
	
	private Thread runnerThread = null;
	
	private boolean[] mouseButtons = new boolean[GFMouseButton.values().length];
	private boolean[] keyboardKeys = new boolean[1024];
	
	/**
	 * Konstruktor za GameFrame, koji se mora pozvati iz naslije?enih klasa
	 * @param title naslov prozora
	 * @param sizeX ?irina u pikselima
	 * @param sizeY visina u pikselima
	 */
	public GameFrame(String title, int sizeX, int sizeY)
	{
		super(true);
		
		if(sizeX < 320) sizeX = 320;
		if(sizeY < 240) sizeY = 240;
		
		if(sizeX > 2048) sizeX = 2048;
		if(sizeY > 2048) sizeY = 2048;
		
		screenX = sizeX;
		screenY = sizeY;
		
		setSize(screenX, screenY);
		
		if(title != null) this.title = title;
		
		this.title = title;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		
		runnerThread = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				while(true)
				{
					long startTime = System.currentTimeMillis();
					if(updatesRunning) tick();
					if(renderRunning) repaint();
					try
					{
						long frameTime = System.currentTimeMillis() - startTime;
						long sleepTime = 1000 / updateRate - frameTime;
						if(sleepTime > 0)
							Thread.sleep(sleepTime);
					} 
					catch (InterruptedException e) { }
				}
			}
		});
		
		//runnerThread.start();
	}
	
	/**
	 * Po?etak rada glavnog threada koji poziva update() i render() metoda, mora
	 * se pozvati kako bi aplikacija po?ela sa radom, najbolje na kraju naslije?enog
	 * konstruktora, nakon ?to se svi resursi u?itaju i pripreme.
	 */
	public void startThread()
	{
		if(!runnerThread.isAlive())
			runnerThread.start();
		else
			System.out.println("Already running!");
	}
	
	private void tick()
	{
		update();
	}
	
	/**
	 * Inicijalizacija prozora (JFrame) u kome se nalazi panel igre, potrebno pozvati
	 * nakon zavr?etka konstruktora.
	 */
	public void initGameWindow()
	{
		if(myFrame != null)
		{
			System.out.println("initGameWindow() already called, can't do again");
			return;
		}
		
		myFrame = new JFrame(title);
		myFrame.setLayout(new BorderLayout());
		
		setPreferredSize(new Dimension(screenX, screenY));
		setMaximumSize(new Dimension(screenX, screenY));
		myFrame.add(this, BorderLayout.CENTER);
		myFrame.setResizable(false);
		myFrame.pack();
		myFrame.setVisible(true);
		myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		myFrame.addKeyListener(this);
		
		myFrame.setIgnoreRepaint(true);
		
		handleWindowInit();
		
		myFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	            public void windowClosing(java.awt.event.WindowEvent evt){
	                handleWindowDestroy();
	                System.exit(0);
	            }
	       });
	}
	
	@Override
    protected void paintComponent(Graphics g)
	{
		if(clearBackBuffer)
		{
			g.setColor(backColor);
			g.fillRect(0, 0, screenX, screenY);
		}
		
		if(useHQ) 
		{
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else
		{
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		
		render((Graphics2D)g, getWidth(), getHeight());
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0)
	{
		GFMouseButton button = GFMouseButton.None;
		if(arg0.getButton() == 1) button = GFMouseButton.Left;
		if(arg0.getButton() == 2) button = GFMouseButton.Middle;
		if(arg0.getButton() == 3) button = GFMouseButton.Right;
		
		if(arg0.getButton() == 5) button = GFMouseButton.Special1;
		if(arg0.getButton() == 4) button = GFMouseButton.Special2;
		
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
		mouseButtons[button.ordinal()] = true;
		handleMouseDown(mouseX, mouseY, button);	
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		GFMouseButton button = GFMouseButton.None;
		if(arg0.getButton() == 1) button = GFMouseButton.Left;
		if(arg0.getButton() == 2) button = GFMouseButton.Middle;
		if(arg0.getButton() == 3) button = GFMouseButton.Right;
		
		if(arg0.getButton() == 5) button = GFMouseButton.Special1;
		if(arg0.getButton() == 4) button = GFMouseButton.Special2;
		
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
		mouseButtons[button.ordinal()] = false;
		handleMouseUp(mouseX, mouseY, button);	
	}

	@Override
	public void keyPressed(KeyEvent arg0)
	{
		if(arg0.getKeyCode() >= 0 && arg0.getKeyCode() < 1024)
			keyboardKeys[arg0.getKeyCode()] = true;
		handleKeyDown(arg0.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0)
	{
		if(arg0.getKeyCode() >= 0 && arg0.getKeyCode() < 1024)
			keyboardKeys[arg0.getKeyCode()] = false;
		handleKeyUp(arg0.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	@Override
	public void mouseDragged(MouseEvent arg0) 
	{
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
		handleMouseMove(mouseX, mouseY);
	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
		handleMouseMove(mouseX, mouseY);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0)
	{
		if(arg0.getWheelRotation() > 0)
		{
			mouseButtons[GFMouseButton.WheelDown.ordinal()] = true;
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			handleMouseDown(mouseX, mouseY, GFMouseButton.WheelDown);
			mouseButtons[GFMouseButton.WheelDown.ordinal()] = false;
		}
		else if(arg0.getWheelRotation() < 0)
		{
			mouseButtons[GFMouseButton.WheelUp.ordinal()] = true;
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			handleMouseDown(mouseX, mouseY, GFMouseButton.WheelUp);
			mouseButtons[GFMouseButton.WheelUp.ordinal()] = false;
		}
	}
	
	/**
	 * Daje trenutni status tipki mi?a 
	 * @param button dugme iz GFMouseButton enuma za koje se tra?i status
	 * @return true ako je pritisnuta, false ako nije
	 */
	protected boolean isMouseButtonDown(GFMouseButton button)
	{
		return mouseButtons[button.ordinal()];
	}
	
	/**
	 * Daje trenutni status tipki tastature 
	 * @param keyCode konstanta iz KeyEvent koja odre?uje tipku
	 * @return true ako je pritisnuta, false ako nije
	 */
	protected boolean isKeyDown(int keyCode)
	{
		if(keyCode >= 0 && keyCode < 1024)
			return keyboardKeys[keyCode];
		else
			return false;
	}
	
	/**
	 * Daje JFrame objekat trenutnog okvira
	 * @return JFrame igre (konstruisan prilikom initGameWindow poziva)
	 */
	protected JFrame getWindow()
	{
		return myFrame;
	}
	
	/**
	 * Pali ili gase automatsko brisanje pozadine prije render() metode
	 * @param clr da li treba raditi clear
	 */
	protected void setBackgroundClear(boolean clr)
	{
		clearBackBuffer = clr;
	}
	
	/**
	 * Postavlja boju na koju ?e pozadina biti postavljena, ako je uklju?en setBackgroundClear
	 * @param c boja
	 */
	protected void setBackgroundClearColor(Color c)
	{
		backColor = c;
	}
	
	/**
	 * Trenutna X koordinata mi?a, u prostoru panela za crtanje
	 * @return X koordinata
	 */
	protected int getMouseX()
	{
		return mouseX;
	}
	
	/**
	 * Trenutna Y koordinata mi?a, u prostoru panela za crtanje
	 * @return Y koordinata
	 */
	protected int getMouseY()
	{
		return mouseY;
	}
	
	/**
	 * Postavlja hint za vi?i kvalitet iscrtavanja koji ?e se onda automatski primjenjivati
	 * nad Graphics2D objektom koji se daje u render() metodi
	 * @param hq true za vi?i kvalitet interpolacije i uklju?en anti-aliasing primitiva
	 */
	protected void setHighQuality(boolean hq)
	{
		useHQ = hq;
	}
	
	/**
	 * Postavlja ikonicu prozora
	 * @param icon Image objekat (mo?e biti BufferedImage)
	 */
	protected void setIcon(Image icon)
	{
		myFrame.setIconImage(icon);
	}
	
	/**
	 * Postavlja ciljnu frekvenciju a?uriranja u Hz/fps. Tajming je realizovan jednostavnim
	 * sleep metodama, zbog ?ega je mogu? neravnomijeran tok izvr?avanja (judder). Ukoliko
	 * a?uriranje i iscrtavanje traje du?e od (1 / fps) sekundi, svukupan tempo izvr?avanja
	 * ?e se usporiti na tu brzinu, nije implementiran nikakav dinami?ki update ili frameskipping.  
	 * @param fps ciljni broj a?uriranih i iscrtanih okvira u sekundi, od 1 do 120
	 */
	protected void setUpdateRate(int fps)
	{
		if(fps >= 1 && fps < 120)
		{
			updateRate = fps;
		}
		else
		{
			System.out.println("Valid range for setUpdateRate is 1 - 120");
		}
	}
	
	/**
	 * Metod ?e biti pozvan prilikom initGameWindow() poziva, nakon ?to je prozor konstruisan.
	 */
	public abstract void handleWindowInit();
	
	/**
	 * Poziva se prilikom ga?enja prozora (ako je korisnik kliknuo na X)
	 */
	public abstract void handleWindowDestroy();
	
	/**
	 * Metod koji treba da obavi kompletno iscrtavanje cijelog frejma, poziva se automatski,
	 * zadatom frekvencijom (update rate). Ne treba da sadr?i nikakva logi?ka a?uriranja, samo crtanje.
	 * @param g Graphics2D objekat preko koga se obavlja crtanje na ekran
	 * @param sw ?irina trenutnog prostora za crtanje
	 * @param sh visina trenutnog prostora za crtanje
	 */
	public abstract void render(Graphics2D g, int sw, int sh);
	
	/**
	 * Metod koji treba da a?urira stanje igre, poziva se prije render() poziva, jednakom frekvencijom.
	 */
	public abstract void update();
	
	/**
	 * Metod koji ?e biti pozvan na pritisak tastera mi?a (okretanja scroll to?ka se tako?e smatraju tasterima) 
	 * @param x X koordinata u pikselima na kojima je kursor bio u trenutku pritiska
	 * @param y Y koordinata u pikselima na kojima je kursor bio u trenutku pritiska
	 * @param button dugme mi?a koje je pritisnuto, iz GFMouseButton enuma
	 */
	public abstract void handleMouseDown(int x, int y, GFMouseButton button);
	
	/**
	 * Metod koji ?e biti pozvan na pu?tanje tastera mi?a (okretanja scroll to?ka se tako?e smatraju tasterima) 
	 * @param x X koordinata u pikselima na kojima je kursor bio u trenutku pu?tanja
	 * @param y Y koordinata u pikselima na kojima je kursor bio u trenutku pu?tanja
	 * @param button dugme mi?a koje je pu?teno, iz GFMouseButton enuma
	 */
	public abstract void handleMouseUp(int x, int y, GFMouseButton button);
	
	/**
	 * Metod koji se poziva pri svakoj promjeni pozicije kursora mi?a, bez obzira da li su tasteri pritisnuti.
	 * @param x X koordinata kursora u pikselima
	 * @param y Y koordinata kursora u pikselima
	 */
	public abstract void handleMouseMove(int x, int y);
	
	/**
	 * Metod koji se poziva kada je pritisnut taster na tastaturi.
	 * @param keyCode kod tipke koja je pritisnuta, porediti sa vrijednostima iz KeyEvent.VK_*
	 */
	public abstract void handleKeyDown(int keyCode);
	
	/**
	 * Metod koji se poziva kada je pu?teni taster na tastaturi.
	 * @param keyCode kod tipke koja je pu?tena, porediti sa vrijednostima iz KeyEvent.VK_*
	 */
	public abstract void handleKeyUp(int keyCode);
}