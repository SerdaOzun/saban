export interface RecordingState {
	audioUrl: string | null;
	audioBlob: Blob | null;
	recording: boolean;
	elapsedTime: number;
}

export interface RecorderOptions {
	maxRecordingTime?: number; // seconds
	onElapsed?: (seconds: number) => void;
	onStop?: (blob: Blob, url: string) => void;
}

export class AudioRecorder {
	private mediaRecorder: MediaRecorder | null = null;
	private chunks: Blob[] = [];
	private stream: MediaStream | null = null;
	private interval: ReturnType<typeof setInterval> | null = null;
	private elapsedTime = 0;
	private recording = false;
	private audioUrl: string | null = null;
	private audioBlob: Blob | null = null;
	private readonly maxRecordingTime: number;
	private readonly onElapsed?: (seconds: number) => void;
	private readonly onStop?: (blob: Blob, url: string) => void;

	constructor(options: RecorderOptions = {}) {
		this.maxRecordingTime = options.maxRecordingTime ?? 10;
		this.onElapsed = options.onElapsed;
		this.onStop = options.onStop;
	}

	get state(): RecordingState {
		return {
			audioUrl: this.audioUrl,
			audioBlob: this.audioBlob,
			recording: this.recording,
			elapsedTime: this.elapsedTime
		};
	}

	async start() {
		this.audioUrl = null;
		this.audioBlob = null;
		this.chunks = [];
		this.elapsedTime = 0;

		this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
		this.mediaRecorder = new MediaRecorder(this.stream);

		this.recording = true;
		this.mediaRecorder.start();

		this.interval = setInterval(() => {
			this.elapsedTime++;
			this.onElapsed?.(this.elapsedTime);
			if (this.elapsedTime >= this.maxRecordingTime) {
				this.stop();
			}
		}, 1000);

		this.mediaRecorder.ondataavailable = (event) => {
			if (event.data.size > 0) this.chunks.push(event.data);
		};

		this.mediaRecorder.onstop = () => {
			if (this.interval) clearInterval(this.interval);
			this.recording = false;
			const blob = new Blob(this.chunks, { type: 'audio/ogg; codecs=opus' });
			const url = URL.createObjectURL(blob);
			this.audioBlob = blob;
			this.audioUrl = url;
			this.onStop?.(blob, url);
		};
	}

	stop() {
		if (this.mediaRecorder && this.recording) {
			this.mediaRecorder.stop();
			this.stream?.getTracks().forEach((t) => t.stop());
		}
	}
}
