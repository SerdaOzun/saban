<script lang="ts">
	import SearchBanner from '../../components/SearchBanner.svelte';
	import { onMount } from 'svelte';
	import { findMatches } from '../../util/Requests.js';
	import { page } from '$app/state';
	import type { SearchResponse } from '../../data/responses/ResponseInterfaces.ts';
	import { Badge } from '$lib/components/ui/badge';
	import { Label } from '$lib/components/ui/label';
	import AudioRecorder from '../../components/AudioRecorder.svelte';
	import * as Tabs from '$lib/components/ui/tabs/index.js';
	import RequestPronunciation from '../../components/RequestPronunciation.svelte';
	import { goto, replaceState } from '$app/navigation';
	import { Button } from '@/components/ui/button';
	import * as Accordion from '@/components/ui/accordion/index.ts';

	let {
		searchText = page.url.searchParams.get('searchText') ?? ''
	}: {
		searchText: string;
	} = $props();

	let lastSearch = $state('');

	let matches: Record<string, SearchResponse[]> = $state({});

	onMount(async () => {
		let result = await findMatches(searchText);
		matches = result ?? {};
		lastSearch = searchText;
	});

	async function onPronunciationSelect(lang: string) {
		await goto(
			`/pronunciations?searchText=${encodeURIComponent(searchText)}&lang=${encodeURIComponent(lang)}`
		);
	}
</script>

<div class="container-saban flex flex-col overflow-auto">
	<SearchBanner largeBanner={false} bind:matches bind:searchTerm={searchText} bind:lastSearch />

	<div class="mt-4 flex flex-col">
		<div class="col-span-1 place-items-center">
			{#if Object.entries(matches).length === 0}
				<div class="mt-2">
					{lastSearch ? `No matches found for '${lastSearch}'` : 'Try a search term'}
				</div>
			{:else}
				<div class="mt-4 flex flex-col gap-2">
					{#each Object.entries(matches) as match}
						<Button
							variant="ghost"
							class="grid grid-cols-2 place-items-center gap-2"
							onclick={() => onPronunciationSelect(match[0])}
						>
							<Badge class="col-span-1">{match[0]}</Badge>
							<Label class="col-span-1"
								>{match[1].length} {match[1].length === 1 ? 'match' : 'matches'}</Label
							>
						</Button>
					{/each}
				</div>
			{/if}
		</div>
	<Accordion.Root type="single" class="w-[350px]" value="closed">
			<Accordion.Item value="item-1">
				<Accordion.Trigger>Request Pronunciation</Accordion.Trigger>
				<Accordion.Content class="">
					<RequestPronunciation word={lastSearch} />
				</Accordion.Content>
			</Accordion.Item>
			<Accordion.Item value="item-2">
				<Accordion.Trigger>Record Audio</Accordion.Trigger>
				<Accordion.Content class="">
					<AudioRecorder word={lastSearch} />
				</Accordion.Content>
			</Accordion.Item>
		</Accordion.Root>
	</div>
</div>

<style>
	.container-saban {
		place-items: center;
		height: 100%;
	}
</style>
