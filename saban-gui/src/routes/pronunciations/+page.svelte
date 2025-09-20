<script lang="ts">
    import SearchBanner from "../../components/SearchBanner.svelte";
    import {onMount} from "svelte";
    import {page} from "$app/state";
    import type {PronunciationResult} from "../../data/responses/ResponseInterfaces.ts";
    import {Label} from "$lib/components/ui/label";
    import {backendUrl} from "../../util/SabanConfig.ts";
    import {Button} from "@/components/ui/button";
    import {Play} from "lucide-svelte";

    let {
        searchText = page.url.searchParams.get('searchText') ?? "",
        lang = page.url.searchParams.get('lang') ?? ""
    }: {
        searchText: string,
        lang: string
    } = $props()

    let pronunciations: Array<PronunciationResult> = $state([])
    let audioElements: Array<HTMLAudioElement> = $state([]);

    onMount(async () => {
        let result = await getPronunciations()
        pronunciations = result ?? []
    })

    async function getPronunciations(): Promise<Array<PronunciationResult>> {
        const response = await fetch(`${backendUrl}/gui/getPronunciations`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            },
            body: JSON.stringify({
                searchText: searchText,
                language: lang
            }),
        });

        if (response.ok) {
            return response.json();
        } else {
            return []
        }

    }

    function formatDate(dateStr: string) {
        return new Date(dateStr).toLocaleString(undefined, {
            dateStyle: 'medium'
        });
    }

    function playAudio(index: number) {
        // Pause any currently playing audio
        audioElements.forEach(audio => {
            if (audio && audio !== audioElements[index]) {
                audio.pause();
                audio.currentTime = 0;
            }
        });

        // Play the selected audio
        if (audioElements[index]) {
            audioElements[index].play();
        }
    }
</script>


<div class="container-saban flex flex-col overflow-auto w-full">
    <SearchBanner largeBanner={false} bind:searchTerm={searchText}/>

    {#if pronunciations.length === 0}
        <Label>No pronunciation found for {searchText}</Label>
    {:else}
    <div class="mt-4 flex flex-col gap-2 w-full place-items-center">
        {#each pronunciations as result, index}
            <div class="flex flex-row place-items-center gap-2 w-max">
                <audio bind:this={audioElements[index]}>
                    <source src={result.url} type="audio/mpeg"/>
                    Your browser does not support the audio element.
                </audio>

                <Button class="rounded-md" variant="outline" onclick={() => playAudio(index)}>
                    <Play/>
                </Button>
                <Label class="text-xl">{result.word}</Label>
                <Label class="text-muted-foreground">by {result.username} [{formatDate(result.createdAt)}]</Label>
            </div>
        {/each}
    </div>
    {/if}
</div>

<style>
    .container-saban {
        place-items: center;
        height: 100%;
    }
</style>