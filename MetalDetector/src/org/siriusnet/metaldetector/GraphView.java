package org.siriusnet.metaldetector;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Draws a graph on the screen. The graph is periodically updated.
 * 
 * @author Cyrill Schenkel
 */
public class GraphView extends View {
	private static final int VALUE_BUFFER_SIZE = 750;
	private static final long UPDATE_PERIOD = 200;
	private static final float GRAPH_STROKE_WIDTH = 2;
	private static final float GRID_STROKE_WIDTH = 1;
	private static final float GRID_TEXT_SIZE = 8;
	private static final int GRAPH_PADDING = 16;

	private int minY;
	private int maxY;
	private int y;
	private int[] valueBuffer;
	private int valueBufferStartPosition;
	private int xWidth;
	private Timer timer;
	private Paint graphLinePaint;
	private Paint graphSurfacePaint;
	private Paint gridPaint;
	private Paint gridAxisLabels;
	private int halfOfStrokeWidthInPx;
	private int graphPaddingInPx;

	/**
	 * Creates a new graph with default settings.
	 * 
	 * @see View#View(Context, AttributeSet)
	 */
	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		minY = 0;
		maxY = 100;
		y = 0;
		valueBuffer = new int[VALUE_BUFFER_SIZE];
		valueBufferStartPosition = 0;
		xWidth = 5;
		timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 0, UPDATE_PERIOD);

		graphLinePaint = new Paint();
		graphLinePaint.setColor(context.getResources().getColor(
				android.R.color.holo_blue_bright));
		graphLinePaint.setStrokeWidth(convertDpToPx(GRAPH_STROKE_WIDTH));
		graphLinePaint.setAntiAlias(true);
		graphLinePaint.setStyle(Style.STROKE);

		graphSurfacePaint = new Paint();
		graphSurfacePaint.setColor(context.getResources().getColor(
				android.R.color.holo_blue_light)
				^ (0xAF << 24));
		graphSurfacePaint.setAntiAlias(true);
		graphSurfacePaint.setStyle(Style.FILL_AND_STROKE);
		halfOfStrokeWidthInPx = convertDpToPx(GRAPH_STROKE_WIDTH / 2);
		graphPaddingInPx = convertDpToPx(GRAPH_PADDING);

		gridPaint = new Paint();
		gridPaint.setColor(android.graphics.Color.LTGRAY);
		gridPaint.setStrokeWidth(convertDpToPx(GRID_STROKE_WIDTH));
		gridPaint.setStyle(Style.STROKE);

		gridAxisLabels = new Paint();
		gridAxisLabels.setColor(Color.BLACK);
		gridAxisLabels.setTextSize(convertDpToPx(GRID_TEXT_SIZE));
		gridAxisLabels.setTextAlign(Align.RIGHT);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		int graphWidth = canvas.getWidth() - 3 * graphPaddingInPx;
		int xMarginLeft = graphPaddingInPx * 2;
		int graphHeight = canvas.getHeight() - 3 * graphPaddingInPx;
		int yMarginTop = graphPaddingInPx;

		int rows = 10;
		int rowHeight = graphHeight / rows;
		int colWidth = xWidth * 4;
		int cols = graphWidth / colWidth;

		graphWidth = cols * colWidth;
		graphHeight = rows * rowHeight;

		float[] gridPoints = new float[(rows + cols + 2) * 4];
		int y;
		// draw the horizontal lines of the coordinate grid and the y axis
		// labels
		for (y = 0; y <= rows + 1; y++) {
			gridPoints[y * 4] = xMarginLeft;
			gridPoints[y * 4 + 1] = yMarginTop + y * rowHeight;
			gridPoints[y * 4 + 2] = xMarginLeft + graphWidth;
			gridPoints[y * 4 + 3] = yMarginTop + y * rowHeight;
			if (y <= rows) {
				canvas.drawText(100 - y * 10 + "%", xMarginLeft
						- convertDpToPx(2), convertDpToPx(GRID_TEXT_SIZE) / 2
						+ yMarginTop + y * rowHeight, gridAxisLabels);
			}
		}
		int aOffset = (y - 1) * 4;
		// draw the vertical lines of the coordinate grid and the x axis labels
		for (int x = 0; x <= cols; x++) {
			gridPoints[aOffset + x * 4] = xMarginLeft + x * colWidth;
			gridPoints[aOffset + x * 4 + 1] = yMarginTop;
			gridPoints[aOffset + x * 4 + 2] = xMarginLeft + x * colWidth;
			gridPoints[aOffset + x * 4 + 3] = yMarginTop + graphHeight;
		}

		canvas.drawLines(gridPoints, gridPaint);

		drawGraph(canvas, graphWidth, xMarginLeft, graphHeight, yMarginTop);
	}

	/**
	 * Draw the graph without the grid.
	 * 
	 * @param canvas
	 *            the canvas to draw the graph
	 * @param graphWidth
	 *            the width of the graph
	 * @param xMarginLeft
	 *            the left margin of the graph
	 * @param graphHeight
	 *            the height of the graph
	 * @param yMarginTop
	 *            the top margin of the graph
	 */
	private void drawGraph(Canvas canvas, int graphWidth, int xMarginLeft,
			int graphHeight, int yMarginTop) {
		Path path = new Path();
		Path surface = new Path();
		surface.moveTo(xMarginLeft, graphHeight + yMarginTop
				+ halfOfStrokeWidthInPx);
		for (int x = 0; graphWidth / xWidth + 1 > x; x++) {
			float xPos = x * xWidth + xMarginLeft;
			float yPos = graphHeight
					+ yMarginTop
					- valueBuffer[(valueBufferStartPosition + x
							+ valueBuffer.length - (graphWidth / xWidth + 1))
							% valueBuffer.length]
					* ((graphHeight + yMarginTop) / 100);
			if (x == 0) {
				path.moveTo(xPos, yPos);
			} else if (x + 1 < graphWidth / xWidth + 1) {
				path.lineTo(xPos, yPos);
			} else {
				path.setLastPoint(xPos, yPos);
			}
			surface.lineTo(xPos, yPos);
		}
		surface.lineTo(graphWidth + xMarginLeft, graphHeight + yMarginTop
				+ halfOfStrokeWidthInPx);
		canvas.drawPath(surface, graphSurfacePaint);
		canvas.drawPath(path, graphLinePaint);
	}

	public int getGraphForeground() {
		return graphSurfacePaint.getColor();
	}

	/**
	 * Set the color of the filled surface of the graph.
	 * 
	 * @param graphForeground
	 *            the surface color
	 */
	public void setGraphForeground(int graphForeground) {
		graphSurfacePaint.setColor(graphForeground);
	}

	public int getGraphLine() {
		return graphLinePaint.getColor();
	}

	/**
	 * Set the line color of the graph.
	 * 
	 * @param graphLine
	 *            the line color
	 */
	public void setGraphLine(int graphLine) {
		graphLinePaint.setColor(graphLine);
	}

	public int getGridColor() {
		return gridPaint.getColor();
	}

	/**
	 * Set the grid color.
	 * 
	 * @param gridColor
	 *            the grid color
	 */
	public void setGridColor(int gridColor) {
		gridPaint.setColor(gridColor);
	}

	/**
	 * Set the y value to draw on the next iteration.
	 * 
	 * @param y
	 *            the current y value
	 */
	public void setYValue(int y) {
		this.y = y;
	}

	public int getMinY() {
		return minY;
	}

	/**
	 * The minimal displayable y value.
	 * 
	 * @param minY
	 *            the minimal y value
	 */
	public void setMinY(int minY) {
		this.minY = minY;
	}

	public int getMaxY() {
		return maxY;
	}

	/**
	 * The maximal displayable y value.
	 * 
	 * @param maxY
	 *            the maximal y value
	 */
	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	public int getxWidth() {
		return xWidth;
	}

	/**
	 * The gap between two x values.
	 * 
	 * @param xWidth
	 *            the gap between two x values
	 */
	public void setxWidth(int xWidth) {
		this.xWidth = xWidth;
	}

	private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if (valueBufferStartPosition == valueBuffer.length) {
				valueBufferStartPosition = 0;
			}
			valueBuffer[valueBufferStartPosition++] = y;
			post(new Runnable() {

				@Override
				public void run() {
					invalidate();
				}
			});
		}
	};

	private int convertDpToPx(float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getContext().getResources().getDisplayMetrics());
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable state = super.onSaveInstanceState();

		SavedGraphViewState saveState = new SavedGraphViewState(state);

		saveState.setValueBufferStartPosition(valueBufferStartPosition);
		saveState.setValueBuffer(valueBuffer);

		return saveState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof SavedGraphViewState) {
			SavedGraphViewState saveState = (SavedGraphViewState) state;
			super.onRestoreInstanceState(saveState.getSuperState());
			valueBufferStartPosition = saveState.getValueBufferStartPosition();
			valueBuffer = saveState.getValueBuffer();
		} else {
			super.onRestoreInstanceState(state);
		}
	}

	static final class SavedGraphViewState extends BaseSavedState {

		private int[] valueBuffer;
		private int valueBufferStartPosition;

		public int[] getValueBuffer() {
			return valueBuffer;
		}

		public void setValueBuffer(int[] valueBuffer) {
			this.valueBuffer = valueBuffer;
		}

		public int getValueBufferStartPosition() {
			return valueBufferStartPosition;
		}

		public void setValueBufferStartPosition(int valueBufferStartPosition) {
			this.valueBufferStartPosition = valueBufferStartPosition;
		}

		public SavedGraphViewState(Parcelable parcelable) {
			super(parcelable);
		}

		public SavedGraphViewState(Parcel in) {
			super(in);
			valueBufferStartPosition = in.readInt();
			in.readIntArray(valueBuffer);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(valueBufferStartPosition);
			dest.writeIntArray(valueBuffer);
		}

		public static final Parcelable.Creator<SavedGraphViewState> CREATOR = new Parcelable.Creator<SavedGraphViewState>() {
			public SavedGraphViewState createFromParcel(Parcel in) {
				return new SavedGraphViewState(in);
			}

			public SavedGraphViewState[] newArray(int size) {
				return new SavedGraphViewState[size];
			}
		};
	}
}