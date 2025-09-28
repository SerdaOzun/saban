<script>
	import Requests from '../components/Requests.svelte';

	import SearchBanner from '../components/SearchBanner.svelte';
</script>

<div class="container-saban flex flex-col">
	<SearchBanner largeBanner={true} />
	<div class="mt-2">
		<Requests />
	</div>
</div>

<style>
	.container-saban {
		place-items: center;
		height: 100%;
	}
</style>
