package com.API.Documents_Management.Init;

import com.API.Documents_Management.Entities.Courriel;
import com.API.Documents_Management.Entities.File;
import com.API.Documents_Management.Repositories.CourrielRepo;
import com.API.Documents_Management.Repositories.FileRepo;
import com.API.Documents_Management.Utils.RandomUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Data

public  class AddRunner {

    private final CourrielRepo courrielRepo;
    private final FileRepo fileRepo;

}