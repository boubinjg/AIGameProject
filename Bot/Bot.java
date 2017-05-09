package bot;
import game.*;
import domineering.*;

import java.util.*;
import java.io.*;

public class Bot extends GamePlayer {
	
	protected ScoredMove[] mvStack;
	public final int MAX_DEPTH = 50;
	public int startDepth;
	public int depthLimit;
	public static final int MAX_SCORE = 1000; // up for debate
	public static Hashtable<Integer, String> transposition = new Hashtable<Integer,String>(1000000,.5f);;
	public Bot(String n, int d) 
	{
		super(n, "Domineering");
		startDepth = d;
		depthLimit = startDepth;
	}
	public long hash(DomineeringState brd)
	{
		long hashKey = 0;
		for(int i = 0; i<8; i++)
			for(int j = 0; j<8; j++)
			{
				if(brd.board[i][j] != DomineeringState.emptySym)
				{
					hashKey += Math.pow(2, i*8+j);
					//System.out.println("i "+ i + " j "+ j + " hashKey "+ hashKey);
				}
			}
		return hashKey;
	}
	public void init()
	{
		mvStack = new ScoredMove[MAX_DEPTH];
		
		//read the file
		String fileName = "transposition.txt";
		String line = null,table = null;;
		try
		{
			FileReader fileReader = new FileReader("src/Bot/"+fileName);
			BufferedReader br = new BufferedReader(fileReader);
			
			while((line = br.readLine())!= null)
			{
				table += line;
			}
			//substring because the first 4 characters are always "null"
			StringTokenizer stcomma = new StringTokenizer(table.substring(4),",");
			while(stcomma.hasMoreTokens())
			{
				//System.out.println(stcomma.nextToken());
				StringTokenizer stequals = new StringTokenizer(stcomma.nextToken(),"=");
				while(stequals.hasMoreTokens())
				{
					transposition.put(Integer.parseInt(stequals.nextToken().trim()), stequals.nextToken());
				}
			}
			//System.out.println(table.substring(4));
			br.close();
			
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		for(int i = 0; i<MAX_DEPTH; i++)
			mvStack[i] = new ScoredMove(0); // move with score of 0 and no position
	}
	public GameMove getMove(GameState brd, String lastMove)
	{
		if(brd.getNumMoves() > 13)
		{
			depthLimit = 15;
		}
		else
			depthLimit = startDepth;
		if(brd.getNumMoves() < 4)
		{
			ScoredMove openingMove = new ScoredMove(0);
			 int rowcol[];
			 int row,col;
			switch(brd.getNumMoves())
			{
				
				case 0:
					rowcol = new int[] {1,6,0,6};
					row = (int)(Math.random()*2);
					col = (int)(Math.random()*2);
					openingMove.set(rowcol[row],rowcol[col+2],rowcol[row],rowcol[col+2]+1,0);
				break;
				case 1:
					while(!brd.moveOK(openingMove))
					{
						rowcol = new int[] {1,6,0,6};
						row = (int)(Math.random()*2);
						col = (int)(Math.random()*2);
						openingMove.set(rowcol[row+2],rowcol[col],rowcol[row+2]+1,rowcol[col],0);
					}
				break;
				case 2:
					while(!brd.moveOK(openingMove))
					{
						rowcol = new int[] {1,6,0,6};
						row = (int)(Math.random()*2);
						col = (int)(Math.random()*2);
						openingMove.set(rowcol[row],rowcol[col+2],rowcol[row],rowcol[col+2]+1,0);
					}
				break;
				case 3:
					while(!brd.moveOK(openingMove))
					{
						rowcol = new int[] {1,6,0,6};
						row = (int)(Math.random()*2);
						col = (int)(Math.random()*2);
						openingMove.set(rowcol[row+2],rowcol[col],rowcol[row+2]+1,rowcol[col],0);
					}
				break;
				/*case 4: 
					while(!brd.moveOK(openingMove))
					{
						rowcol = new int[] {1,6};
						row = (int)(Math.random()*2);
						col = (int)(Math.random()*4) * 2 ;
						openingMove.set(rowcol[row],col,rowcol[row],col+1,0);
					}
				break;
				case 5:
					while(!brd.moveOK(openingMove))
					{
						rowcol = new int[] {1,6};
						col = (int)(Math.random()*2);
						row = (int)(Math.random()*4);
						openingMove.set(row*2, rowcol[col], row*2+1, rowcol[col],0);
					}
				break;
				/*case 6:
					while(!brd.moveOK(openingMove))
					{
						rowcol = new int[] {1,6};
						row = (int)(Math.random()*2);
						col = (int)(Math.random()*4) * 2 ;
						openingMove.set(rowcol[row],col,rowcol[row],col+1,0);
					}
				break;
				case 7:
					while(!brd.moveOK(openingMove))
					{
						rowcol = new int[] {1,6};
						col = (int)(Math.random()*2);
						row = (int)(Math.random()*4);
						openingMove.set(row*2, rowcol[col], row*2+1, rowcol[col],0);
					}
				break;*/
			}
			return openingMove;
		}
		else
		{
			//create a key by hashing the board position
			int key = (int)(hash((DomineeringState)brd)%1000000); 
			
			/*if you have seen the hash generated above, make the move it suggests, otherwise alpha beta*/
			String value = transposition.get(key);
			//if the hash table has a value at that key, set the move to be the move it provides
			ScoredMove move = new ScoredMove(0);
			int moveDepth=1000000;
			
			if(value!=null)
			{
				//System.out.println("transposition hit");
				StringTokenizer st = new StringTokenizer(value);
				move.set(Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()),
						 Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()),
						 Double.parseDouble(st.nextToken()));
				moveDepth = Integer.parseInt(st.nextToken());
			}
			if(value != null && brd.moveOK(move) && moveDepth == brd.getNumMoves())
				return move;
			else
			{
				//System.out.println("alpha beta/transposition add");
				alphaBeta((DomineeringState)brd, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
				
				if(value != null)
				{	
					if(brd.getNumMoves() > moveDepth)
					{	
						transposition.remove(key);
						transposition.put(key, mvStack[0].toString()+" "+brd.getNumMoves());
					}
					System.out.println("collision");
				}
				else
					transposition.put(key, mvStack[0].toString()+" "+brd.getNumMoves());
				
				return mvStack[0];
		}
		}
	}
	public static int evalBoard(DomineeringState brd, int dr, int dc)
	{
		int count = 0;

		//position
		for(int i = 0; i < DomineeringState.ROWS-dr; i++)
			for(int j = 0; j<DomineeringState.COLS-dc; j++)
			{
				//checking the number of valid spots
				if(brd.board[i][j] == DomineeringState.emptySym && 
				   brd.board[i+dr][j+dc] == DomineeringState.emptySym)
				{
					if(dr == 0)
					{
						count++;
					
						if(((i==7) || (brd.board[i+1][j] != DomineeringState.emptySym && brd.board[i+1][j+1] != DomineeringState.emptySym))
						   && ((i==0) || (brd.board[i-1][j] != DomineeringState.emptySym && brd.board[i-1][j+1] != DomineeringState.emptySym)))
						{
							count+= 2;
						}
					}
					//i max 6, j max 7
					if(dr == 1)
					{	
						count--;
					   
						if(((j == 7) || (brd.board[i][j+1] != DomineeringState.emptySym && brd.board[i+1][j+1] != DomineeringState.emptySym))
						   && ((j == 0) || (brd.board[i][j-1] != DomineeringState.emptySym && brd.board[i+1][j-1] != DomineeringState.emptySym)))
						{
							count-=2;
						}
					}
				}
		}
		
		for(int i = 0; i < DomineeringState.ROWS-dc; i++)
			for(int j = 0; j<DomineeringState.COLS-dr; j++)
			{
				//checking the number of valid spots
				if(brd.board[i][j] == DomineeringState.emptySym && 
				   brd.board[i+dc][j+dr] == DomineeringState.emptySym)
				{
					if(dr == 0)
					{
						count--;
					
						if(((i==7) || (brd.board[i+1][j] != DomineeringState.emptySym && brd.board[i+1][j+1] != DomineeringState.emptySym))
						   && ((i==0) || (brd.board[i-1][j] != DomineeringState.emptySym && brd.board[i-1][j+1] != DomineeringState.emptySym)))
						{
							count-= 2;
						}
					}
					//i max 6, j max 7
					if(dr == 1)
					{	
					   count++;
					   if( ((j == 7) || (brd.board[i][j+1] != DomineeringState.emptySym && brd.board[i+1][j+1] != DomineeringState.emptySym))
					   && ((j == 0) || (brd.board[i][j-1] != DomineeringState.emptySym && brd.board[i+1][j-1] != DomineeringState.emptySym)))
					   {
						   count += 2;
					   }
					}
				}
		}
		
		return count;                                                                                                                                                                                                                                                                                                                                                           
	}
	protected class ScoredMove extends DomineeringMove
	{
		public double score;
		public ScoredMove(double score)
		{
			super();
		}
		public void set(int r1, int c1, int r2, int c2, double s)
		{
			row1=r1;
			row2=r2;
			col1=c1;
			col2=c2;
			score=s;		
		}
		public String toString()
		{
			return row1 + " " + col1 + " "	+ row2 + " " + col2 + " " + score;	
		}
	}
	public boolean terminalValue(DomineeringState brd, ScoredMove mv)
	{
		//essentially just returns of the game is over.
		GameState.Status status = brd.getStatus(); 
		
		boolean isTerminal = true;
		//states 
		//GAME_ON
		//HOME_WIN
		//AWAY_WIN
		if(status == GameState.Status.HOME_WIN)
			mv.set(0,0,0,0,MAX_SCORE);
		else if(status == GameState.Status.AWAY_WIN)
			mv.set(0,0,0,0,-MAX_SCORE);
		else
			isTerminal = false;
		return isTerminal;
	}
	public void shuffle(int[] ary){
		int len = ary.length;
		for (int i = 0; i < len; i++) {
			int spot = Util.randInt(i, len - 1);
			int tmp = ary[i];
			ary[i] = ary[spot];
			ary[spot] = tmp;
		}
	}
	
	public void alphaBeta(DomineeringState brd, int currDepth, double alpha, double beta)
	{
		int dc, dr;
		if (brd.getWho() == GameState.Who.HOME) 
		{
                        dr = 0;
                        dc = 1;
                } 
		else 
		{
                        dr = 1;
                        dc = 0;
                }


		//decides who to maximize and minimize
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;
		
		//finds the state of the game
		boolean isTerminal = terminalValue(brd,mvStack[currDepth]);
		
		
		if(isTerminal)
		{
			;
		}
		else if(currDepth == depthLimit)
		{
			//evalBoard doesn't exist yet, not sure about the location either
			mvStack[currDepth].set(0,0,0,0,evalBoard(brd, dr, dc));
		}
		else
		{
			//alpha beta
			ScoredMove tempMv = new ScoredMove(0);//just the score, may mot be correct
			double bestScore = (toMaximize ?
						Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			ScoredMove bestMove = mvStack[currDepth];
			ScoredMove nextMove = mvStack[currDepth+1];

			bestMove.set(0,0,0,0,bestScore);
			
			GameState.Who currTurn = brd.getWho();

			//randomize the row or column chosen 
			//do that right here
			int[] columns = new int[DomineeringState.COLS];
			for(int a = 0; a < DomineeringState.COLS; a++)
				columns[a] = a;
			shuffle(columns);
			
			int[] rows = new int[DomineeringState.ROWS];
			for(int a = 0; a < DomineeringState.ROWS; a++)
				rows[a] = a;
			shuffle(rows);
	
			for (int i=0; i<DomineeringState.COLS; i++) 
			{
				for(int j = 0; j<DomineeringState.ROWS; j++)
				{	 
					
					int randRow = rows[i];
					int randCol = columns[j];
					
					tempMv.row1 = randRow; // initialize move 
					tempMv.row2 = randRow+dr;
					tempMv.col1 = randCol;
					tempMv.col2 = randCol+dc;
					if(brd.moveOK(tempMv))
					{	
						brd.makeMove(tempMv); 
						alphaBeta(brd, currDepth+1, alpha, beta);  // Check out move 

						// Undo move 
						//brd.numInCol[c]--a; 
						//int row = brd.numInCol[c]; 
						brd.board[randRow][randCol] = DomineeringState.emptySym; 
						brd.board[randRow+dr][randCol+dc] = DomineeringState.emptySym;
						brd.numMoves--;
						brd.status = GameState.Status.GAME_ON;
						brd.who = currTurn; 

						// Check out the results, relative to what we've seen before 
						if (toMaximize && nextMove.score > bestMove.score) 
						{	 
							bestMove.set(randRow,randCol,randRow+dr,randCol+dc, nextMove.score); 
						} 	
						else if (!toMaximize && nextMove.score < bestMove.score) 
						{ 
							bestMove.set(randRow,randCol,randRow+dr,randCol+dc, nextMove.score); 
						} 

						// Update alpha and beta. Perform pruning, if possible. 

						if (toMinimize) 
						{ 
							beta = Math.min(bestMove.score, beta); 
							if(bestMove.score <= alpha || bestMove.score == -MAX_SCORE)
							{ 
								return;
							}
						} 
						else 
						{	 
							alpha = Math.max(bestMove.score, alpha); 
							if (bestMove.score >= beta || bestMove.score == MAX_SCORE) 
							{ 
								return;
								//skynet--
							} 
						}
					}
				}
			}
		}
	}
	public void printTransposition()
	{
		
	}
	public static void main(String [] args)
	{
		GamePlayer bot = new Bot("bot",8);
		bot.compete(args, 0);
		
		/* write the new transposition table*/
		PrintWriter writer;
		try {
			writer = new PrintWriter("transposition.txt");
			
			String s = transposition.toString();
			s = s.substring(1);
			s = s.replace("}", "");
			writer.print(s);
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();

		}
	}
}

